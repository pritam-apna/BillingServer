package com.example.authserver.config;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${app.rabbitmq.auth-exchange:auth-exchange}")
    private String authExchangeName;

    @Bean
    public TopicExchange authExchange() {
        return new TopicExchange(authExchangeName);
    }

    @Bean
    public org.springframework.amqp.core.Queue authQueue() {
        return new org.springframework.amqp.core.Queue("auth_queue");
    }

    @Bean
    public org.springframework.amqp.core.Binding bindingAuthQueue(org.springframework.amqp.core.Queue authQueue, TopicExchange authExchange) {
        return org.springframework.amqp.core.BindingBuilder.bind(authQueue).to(authExchange).with("auth.user.#");
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
