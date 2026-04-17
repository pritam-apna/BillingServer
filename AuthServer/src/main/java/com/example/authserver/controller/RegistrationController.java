package com.example.authserver.controller;

import com.example.authserver.entity.User;
import com.example.authserver.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.UUID;

@RestController
@RequestMapping("/api/register")
public class RegistrationController {

    private final UserRepository userRepository;
    private final RegisteredClientRepository registeredClientRepository;
    private final PasswordEncoder passwordEncoder;

    public RegistrationController(UserRepository userRepository,
                                  RegisteredClientRepository registeredClientRepository,
                                  PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.registeredClientRepository = registeredClientRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/user")
    public ResponseEntity<String> registerUser(@RequestBody UserRequest userRequest) {
        if (userRepository.findByUsername(userRequest.username()).isPresent()) {
            return ResponseEntity.badRequest().body("Username already exists");
        }
        User user = new User();
        user.setUsername(userRequest.username());
        user.setPassword(passwordEncoder.encode(userRequest.password()));
        user.setRoles(Collections.singleton("USER"));
        userRepository.save(user);
        return ResponseEntity.ok("User registered successfully");
    }

    @PostMapping("/client")
    public ResponseEntity<String> registerClient(@RequestBody ClientRequest clientRequest) {
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
        return ResponseEntity.ok("Client registered successfully");
    }

    public record UserRequest(String username, String password) {}
    public record ClientRequest(String clientId, String clientSecret, String redirectUri, String postLogoutRedirectUri, String scope) {}
}
