package com.example.authserver.messaging;

/**
 * Interface defining the events that can be published regarding users.
 * This abstracts away the underlying message broker implementation.
 */
public interface AuthEventPublisher {
    
    /**
     * Publishes an event when a new user is created.
     */
    void publishUserCreated(UserEvent event);

    /**
     * Publishes an event when a user's details are updated.
     */
    void publishUserUpdated(UserEvent event);

    /**
     * Publishes an event when a user successfully logs in.
     */
    void publishUserLoggedIn(UserEvent event);
}
