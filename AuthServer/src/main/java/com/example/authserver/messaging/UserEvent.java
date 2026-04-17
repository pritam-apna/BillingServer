package com.example.authserver.messaging;

import java.util.Set;

/**
 * Event payload representing a User without sensitive information like passwords.
 * 
 * @param id The internal user ID
 * @param username The user's username
 * @param roles The roles granted to the user
 * @param eventType The type of event (e.g., CREATED, UPDATED, LOGGED_IN)
 */
public record UserEvent(
        Long id,
        String username,
        Set<String> roles,
        String eventType
) {}
