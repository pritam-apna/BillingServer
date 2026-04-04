package com.example.inventoryserver.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authorize -> authorize
                // Allow H2 console for debugging
                .requestMatchers("/h2-console/**").permitAll()
                // All API endpoints require a valid JWT Bearer token
                .requestMatchers("/api/**").hasAnyAuthority("SCOPE_inventory.read", "SCOPE_inventory.write")
                .anyRequest().authenticated()
            )
            // Validate Bearer tokens against the AuthServer's JWK endpoint
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> {}))
            // Allow H2 frames
            .headers(headers -> headers.frameOptions(frame -> frame.disable()));

        return http.build();
    }
}
