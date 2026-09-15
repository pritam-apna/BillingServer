package com.example.authserver.dto;

import java.util.Set;

public record UserRequest(String username, String password, String email, String phone, Set<String> authorizedClientIds) {}
