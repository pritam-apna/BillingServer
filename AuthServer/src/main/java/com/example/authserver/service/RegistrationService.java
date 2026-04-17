package com.example.authserver.service;

import com.example.authserver.dto.ClientRequest;
import com.example.authserver.dto.UserRequest;
import com.example.authserver.entity.User;
import com.example.authserver.messaging.AuthEventPublisher;
import com.example.authserver.messaging.UserEvent;
import com.example.authserver.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

@Service
public class RegistrationService {

    private final UserRepository userRepository;
    private final RegisteredClientRepository registeredClientRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthEventPublisher authEventPublisher;

    public RegistrationService(UserRepository userRepository,
                               RegisteredClientRepository registeredClientRepository,
                               PasswordEncoder passwordEncoder,
                               AuthEventPublisher authEventPublisher) {
        this.userRepository = userRepository;
        this.registeredClientRepository = registeredClientRepository;
        this.passwordEncoder = passwordEncoder;
        this.authEventPublisher = authEventPublisher;
    }

    public boolean registerUser(UserRequest userRequest) {
        if (userRepository.findByUsername(userRequest.username()).isPresent()) {
            return false;
        }

        User user = new User();
        user.setUsername(userRequest.username());
        user.setPassword(passwordEncoder.encode(userRequest.password()));
        user.setRoles(Collections.singleton("USER"));
        user.setEmail(userRequest.email());
        user.setPhone(userRequest.phone());
        User savedUser = userRepository.save(user);

        UserEvent event = new UserEvent(savedUser.getId(), savedUser.getUsername(), savedUser.getRoles(), savedUser.getEmail(), savedUser.getPhone(), "CREATED");
        authEventPublisher.publishUserCreated(event);

        return true;
    }

    public boolean updateUser(Long id, UserRequest userRequest) {
        Optional<User> existingUserOpt = userRepository.findById(id);
        if (existingUserOpt.isEmpty()) {
            return false;
        }

        User existingUser = existingUserOpt.get();
        existingUser.setUsername(userRequest.username());
        
        if (userRequest.password() != null && !userRequest.password().trim().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(userRequest.password()));
        }

        User updatedUser = userRepository.save(existingUser);

        UserEvent event = new UserEvent(updatedUser.getId(), updatedUser.getUsername(), updatedUser.getRoles(),updatedUser.getEmail(),updatedUser.getPhone(), "UPDATED");
        authEventPublisher.publishUserUpdated(event);

        return true;
    }

    public void registerClient(ClientRequest clientRequest) {
        RegisteredClient registeredClient = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId(clientRequest.clientId())
                .clientSecret(passwordEncoder.encode(clientRequest.clientSecret()))
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .redirectUri(clientRequest.redirectUri())
                .postLogoutRedirectUri(clientRequest.postLogoutRedirectUri())
                .scope("openid")
                .scope("profile")
                .scope(clientRequest.scope())
                .clientSettings(ClientSettings.builder().requireAuthorizationConsent(true).build())
                .build();

        registeredClientRepository.save(registeredClient);
    }
}
