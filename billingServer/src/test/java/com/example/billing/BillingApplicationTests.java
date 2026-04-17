package com.example.billing;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class BillingApplicationTests {

    @MockitoBean
    ClientRegistrationRepository clientRegistrationRepository;

    @MockitoBean
    OAuth2AuthorizedClientManager oAuth2AuthorizedClientManager;

    @Test
    void contextLoads() {
    }
}
