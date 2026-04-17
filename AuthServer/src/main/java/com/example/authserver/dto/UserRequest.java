package com.example.authserver.dto;

public record UserRequest(String username, String password, String email, String phone) {}
