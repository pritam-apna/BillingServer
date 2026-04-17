package com.example.billing.messaging;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "billing_inventory_exchange";

    public static final String BILLING_QUEUE = "billing_queue";
    public static final String SALE_ROUTING_KEY = "product.created";

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    @Bean
    public Queue billingQueue() {
        return new Queue(BILLING_QUEUE);
    }

    @Bean
    public Binding bindingBillingQueue(Queue billingQueue, TopicExchange exchange) {
        return BindingBuilder.bind(billingQueue).to(exchange).with(SALE_ROUTING_KEY);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
