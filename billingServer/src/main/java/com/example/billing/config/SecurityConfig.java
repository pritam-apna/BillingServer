package com.example.billing.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final ClientRegistrationRepository clientRegistrationRepository;

    public SecurityConfig(ClientRegistrationRepository clientRegistrationRepository) {
        this.clientRegistrationRepository = clientRegistrationRepository;
    }

    private OidcClientInitiatedLogoutSuccessHandler oidcLogoutSuccessHandler() {
        OidcClientInitiatedLogoutSuccessHandler oidcLogoutSuccessHandler =
                new OidcClientInitiatedLogoutSuccessHandler(this.clientRegistrationRepository);
        
        // Post logout redirect to root URL, perfectly matching the AuthServer's allowed postLogoutRedirectUri
        oidcLogoutSuccessHandler.setPostLogoutRedirectUri("{baseUrl}/");
        return oidcLogoutSuccessHandler;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authorize -> authorize
                // Secure Admin UI and Admin APIs
                .requestMatchers("/admin/**", "/api/admin/**", "/admin").authenticated()
                // Require Auth for POS system as well (optional, but typical)
                .requestMatchers("/new-bill", "/invoices/**").authenticated()
                // Open resources
                .requestMatchers("/css/**", "/js/**", "/h2-console/**").permitAll()
                .anyRequest().permitAll()
            )
            .oauth2Login(withDefaults())
            .logout(logout -> logout
                .logoutSuccessHandler(oidcLogoutSuccessHandler())
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID")
            );

        // Allow H2 database console iframe
        http.headers(headers -> headers.frameOptions(frame -> frame.disable()));
        
        // Disable CSRF for local testing of REST APIs via Javascript
        http.csrf(csrf -> csrf.disable());

        return http.build();
    }
}
