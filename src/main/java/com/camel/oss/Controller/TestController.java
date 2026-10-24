package com.camel.oss.Controller;

import org.apache.camel.ProducerTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.UUID;


@RestController
public class TestController {

    @Autowired
    private ProducerTemplate producerTemplate;

    @GetMapping("/trigger")
    public String triggerRoute(@RequestParam String message) {
        // Pushes the message into the direct endpoint
        producerTemplate.sendBody("direct:Subscribe", message);
        return "Message sent to Camel route! "+message;
    }

    @GetMapping("/publish")
    public String publishToQueue(@RequestParam String message) {
        // 1. Generate the unique ID
        String traceId = UUID.randomUUID().toString();

        // 2. Pass it to Camel (usually in a Header, not the body)
        producerTemplate.sendBodyAndHeader("direct:publish", message, "myTraceId", traceId);

        return "Message published with Trace ID: " + traceId;
    }

    @GetMapping("/read")
    public String readFromQueue() {
        // Triggers the direct:read endpoint which reads a message from RabbitMQ queue
        String message = producerTemplate.requestBody("direct:read", null, String.class);
        return "Message read from queue: "+message;
    }
}
