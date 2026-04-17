package com.example.authserver.dto;

public record ClientRequest(String clientId, String clientSecret, String redirectUri, String postLogoutRedirectUri, String scope) {}
