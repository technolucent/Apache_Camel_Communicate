package com.camel.oss.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitMqConfig {

    public static final String QUEUE = "hello";
    public static final String DLQ = "hello.dlq";
    public static final String DLX = "hello.dlx";

    @Bean
    public Queue helloQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", DLX);
        args.put("x-dead-letter-routing-key", DLQ);
        return new Queue(QUEUE, true, false, false, args);
    }

    @Bean
    public Queue dlq() {
        return new Queue(DLQ, true, false, false);
    }

    @Bean
    public DirectExchange helloExchange() {
        return new DirectExchange("hello");
    }

    @Bean
    public DirectExchange dlx() {
        return new DirectExchange(DLX);
    }

    @Bean
    public Binding binding(Queue helloQueue, DirectExchange helloExchange) {
        return BindingBuilder.bind(helloQueue).to(helloExchange).with("hello");
    }

    @Bean
    public Binding dlqBinding(Queue dlq, DirectExchange dlx) {
        return BindingBuilder.bind(dlq).to(dlx).with(DLQ);
    }
}