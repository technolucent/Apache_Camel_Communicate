package com.camel.oss.router;

import java.util.ArrayList;
import java.util.List;

import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
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
            .to("spring-rabbitmq:hello?routingKey=hello&arg.queue.durable=true&arg.queue.auto-delete=false")
            .doTry()
                .to("spring-rabbitmq:hello?routingKey=hello&arg.queue.durable=true&arg.queue.auto-delete=false")
            .doCatch(Exception.class)
                .log(LoggingLevel.ERROR, "Publish failed: ${exception.message}")
                .setBody().constant("Publish failed")
            .end();

        // Read a message from RabbitMQ queue via API trigger
        from("direct:read")
            .routeId("RouteRead")
            .process(this::readMessage)
            .log("Read message from queue: ${body}");

        // Drain all currently available messages from the queue
        from("direct:drain")
            .routeId("RouteDrain")
            .process(this::drainMessages)
            .log("Drained ${body.size} message(s) from queue");
    }

    private void readMessage(Exchange exchange) {
        try {
            Message message = rabbitTemplate.receive(RabbitMqConfig.QUEUE, 1000);
            if (message != null) {
                exchange.getMessage().setBody(new String(message.getBody()));
            } else {
                exchange.getMessage().setBody("No messages in queue");
            }
        } catch (Exception e) {
            exchange.getMessage().setBody("Error reading from queue: " + e.getMessage());
        }
    }

    private void drainMessages(Exchange exchange) {
        List<String> messages = new ArrayList<>();
        try {
            Message message;
            int maxMessages = 100; // Prevent infinite loop
            int count = 0;
            while (count < maxMessages && (message = rabbitTemplate.receive(RabbitMqConfig.QUEUE, 200)) != null) {
                messages.add(new String(message.getBody()));
                count++;
            }
        } catch (Exception e) {
            messages.add("Error draining queue: " + e.getMessage());
        }
        if (messages.isEmpty()) {
            messages.add("No messages in queue");
        }
        exchange.getMessage().setBody(messages);
    }
}