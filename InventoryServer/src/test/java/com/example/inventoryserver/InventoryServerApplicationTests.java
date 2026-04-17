package com.example.inventoryserver;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class InventoryServerApplicationTests {

    @org.junit.jupiter.api.BeforeAll
    static void setup() {
        java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("UTC"));
    }

    @MockitoBean
    JwtDecoder jwtDecoder;

    @MockitoBean
    ClientRegistrationRepository clientRegistrationRepository;

	@Test
	void contextLoads() {
	}

}
