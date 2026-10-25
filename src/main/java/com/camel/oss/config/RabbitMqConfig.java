package com.camel.oss.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    public static final String QUEUE = "hello";

    @Bean
    public Queue helloQueue() {
        return new Queue(QUEUE, true, false, false);
    }

    @Bean
    public DirectExchange helloExchange() {
        return new DirectExchange("hello");
    }

    @Bean
    public Binding binding(Queue helloQueue, DirectExchange helloExchange) {
        return BindingBuilder.bind(helloQueue).to(helloExchange).with("hello");
    }
}