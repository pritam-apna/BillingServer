package com.example.authserver.security;

import com.example.authserver.entity.User;
import com.example.authserver.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationCodeRequestAuthenticationContext;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationCodeRequestAuthenticationException;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationCodeRequestAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Component
public class ClientAuthorizationValidator implements Consumer<OAuth2AuthorizationCodeRequestAuthenticationContext> {

    private final UserRepository userRepository;

    public ClientAuthorizationValidator(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void accept(OAuth2AuthorizationCodeRequestAuthenticationContext context) {
        OAuth2AuthorizationCodeRequestAuthenticationToken authorizationCodeRequestAuthentication =
                context.getAuthentication();
        
        String clientId = authorizationCodeRequestAuthentication.getClientId();
        Authentication principal = (Authentication) authorizationCodeRequestAuthentication.getPrincipal();

        if (principal == null || !principal.isAuthenticated()) {
            return;
        }

        String username = principal.getName();
        User user = userRepository.findByUsername(username).orElse(null);

        if (user != null) {
            if (user.getAuthorizedClientIds() == null || !user.getAuthorizedClientIds().contains(clientId)) {
                OAuth2Error error = new OAuth2Error(OAuth2ErrorCodes.ACCESS_DENIED,
                        "User is not authorized for this client: " + clientId, null);
                throw new OAuth2AuthorizationCodeRequestAuthenticationException(error, null);
            }
        }
    }
}
