package com.camel.oss.Controller;

import org.apache.camel.ProducerTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class TestController {

    @Autowired
    private ProducerTemplate producerTemplate;

    @GetMapping("/trigger")
    public String triggerRoute(@RequestParam String message) {
        if (message == null || message.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "message must not be blank");
        }
        // Pushes the message into the direct endpoint
        producerTemplate.sendBody("direct:Subscribe", message);
        return "Message sent to Camel route! " + message;
    }

    @GetMapping("/publish")
    public String publishToQueue(@RequestParam String message) {
        if (message == null || message.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "message must not be blank");
        }
        // Publish through Camel route (tracing handled by OpenTelemetry agent)
        producerTemplate.sendBody("direct:publish", message);
        return "Message published successfully.";
    }

    @GetMapping("/read")
    public String readFromQueue() {
        // Triggers the direct:read endpoint which reads a message from RabbitMQ queue
        String message = producerTemplate.requestBody("direct:read", null, String.class);
        return "Message read from queue: " + message;
    }

    @GetMapping("/drain")
    public Object drainQueue() {
        // Triggers the direct:drain endpoint which drains all available messages
        return producerTemplate.requestBody("direct:drain", (Object) null, Object.class);
    }
}