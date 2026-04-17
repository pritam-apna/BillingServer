package com.example.authserver.messaging;

import com.example.authserver.entity.User;
import com.example.authserver.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AuthenticationEventListener implements ApplicationListener<AuthenticationSuccessEvent> {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationEventListener.class);
    
    private final AuthEventPublisher authEventPublisher;
    private final UserRepository userRepository;

    public AuthenticationEventListener(AuthEventPublisher authEventPublisher, UserRepository userRepository) {
        this.authEventPublisher = authEventPublisher;
        this.userRepository = userRepository;
    }

    @Override
    public void onApplicationEvent(AuthenticationSuccessEvent event) {
        String username = event.getAuthentication().getName();
        
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            UserEvent userEvent = new UserEvent(user.getId(), user.getUsername(), user.getRoles(), "LOGGED_IN");
            authEventPublisher.publishUserLoggedIn(userEvent);
            log.info("Successfully published login event for user: {}", username);
        } else {
            // This might occur if a client authenticates via Client Credentials, not a user.
            log.debug("Authentication success event triggered but user '{}' not found in database. Likely a client login.", username);
        }
    }
}
