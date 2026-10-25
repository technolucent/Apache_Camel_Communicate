package com.camel.oss.router;

import java.util.ArrayList;
import java.util.List;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.camel.oss.config.RabbitMqConfig;

@Component
public class RouteB extends RouteBuilder {

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
                Message message = rabbitTemplate.receive(RabbitMqConfig.QUEUE, 1000);
                if (message != null) {
                    exchange.getMessage().setBody(new String(message.getBody()));
                } else {
                    exchange.getMessage().setBody("No messages in queue");
                }
            })
            .log("Read message from queue: ${body}");

        // Drain all currently available messages from the queue
        from("direct:drain")
            .routeId("RouteDrain")
            .process(exchange -> {
                List<String> messages = new ArrayList<>();
                Message message;
                while ((message = rabbitTemplate.receive(RabbitMqConfig.QUEUE, 200)) != null) {
                    messages.add(new String(message.getBody()));
                }
                if (messages.isEmpty()) {
                    messages.add("No messages in queue");
                }
                exchange.getMessage().setBody(messages);
            })
            .log("Drained ${body.size} message(s) from queue");
    }
}