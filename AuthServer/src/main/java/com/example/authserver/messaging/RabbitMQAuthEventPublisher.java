package com.example.authserver.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "app.eventing.enabled", havingValue = "true")
public class RabbitMQAuthEventPublisher implements AuthEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(RabbitMQAuthEventPublisher.class);
    private final RabbitTemplate rabbitTemplate;

    @Value("${app.rabbitmq.auth-exchange:auth-exchange}")
    private String authExchange;

    public RabbitMQAuthEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void publishUserCreated(UserEvent event) {
        log.info("Publishing user created event for user: {}", event.username());
        rabbitTemplate.convertAndSend(authExchange, "auth.user.created", event);
    }

    @Override
    public void publishUserUpdated(UserEvent event) {
        log.info("Publishing user updated event for user: {}", event.username());
        rabbitTemplate.convertAndSend(authExchange, "auth.user.updated", event);
    }

    @Override
    public void publishUserLoggedIn(UserEvent event) {
        log.info("Publishing user logged in event for user: {}", event.username());
        rabbitTemplate.convertAndSend(authExchange, "auth.user.loggedin", event);
    }
}
