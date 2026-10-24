package com.camel.oss.router;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.amqp.core.Message;

import java.util.ArrayList;
import java.util.List;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class RouteB extends RouteBuilder {
    private static final String QUEUE = "hello";

    @Bean
    public org.springframework.amqp.core.Queue helloQueue() {
        return new org.springframework.amqp.core.Queue(QUEUE, true, false, false);
    }

    @Bean
    public org.springframework.amqp.core.DirectExchange helloExchange() {
        return new org.springframework.amqp.core.DirectExchange("hello");
    }

    @Bean
    public org.springframework.amqp.core.Binding binding(org.springframework.amqp.core.Queue helloQueue, org.springframework.amqp.core.DirectExchange helloExchange) {
        return org.springframework.amqp.core.BindingBuilder.bind(helloQueue).to(helloExchange).with("hello");
    }

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Override
    public void configure() throws Exception {
        // Publish message to RabbitMQ queue via API trigger
        from("direct:publish")
            .routeId("RoutePublish")
            .log("Publishing message to queue: ${body}")
            .to("spring-rabbitmq:hello?routingKey=hello&arg.queue.durable=true&arg.queue.auto-delete=false");

        // Read a message from RabbitMQ queue via API trigger
        from("direct:read")
            .routeId("RouteRead")
            .process(exchange -> {
                try {
                    Message message = rabbitTemplate.receive(QUEUE, 1000);
                    if (message != null) {
                        String body = new String(message.getBody());
                        exchange.getMessage().setBody(body);
                    } else {
                        exchange.getMessage().setBody("No messages in queue");
                    }
                } catch (Exception e) {
                    exchange.getMessage().setBody("Error reading from queue: " + e.getMessage());
                }
            })
            .log("Read message from queue: ${body}");

        // Drain all currently available messages from the queue
        from("direct:drain")
            .routeId("RouteDrain")
            .process(exchange -> {
                List<String> messages = new ArrayList<>();
                try {
                    Message message;
                    while ((message = rabbitTemplate.receive(QUEUE, 200)) != null) {
                        messages.add(new String(message.getBody()));
                    }
                    if (messages.isEmpty()) {
                        messages.add("No messages in queue");
                    }
                } catch (Exception e) {
                    messages.add("Error draining queue: " + e.getMessage());
                }
                exchange.getMessage().setBody(messages);
            })
            .log("Drained ${body.size} message(s) from queue");
    }
}