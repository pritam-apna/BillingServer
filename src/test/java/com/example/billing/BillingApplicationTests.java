package com.example.billing;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

@SpringBootTest
class BillingApplicationTests {

    @MockitoBean
    ClientRegistrationRepository clientRegistrationRepository;

	@Test
	void contextLoads() {
	}

}
