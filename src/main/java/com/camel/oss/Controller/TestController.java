package com.camel.oss.Controller;

import org.apache.camel.ProducerTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@RestController
@Validated
public class TestController {

    @Autowired
    private ProducerTemplate producerTemplate;

    @GetMapping("/trigger")
    public String triggerRoute(@RequestParam @NotBlank @Size(max = 10000) String message) {
        producerTemplate.sendBody("direct:Subscribe", message);
        return "Message sent to Camel route! " + message;
    }

    @GetMapping("/publish")
    public String publishToQueue(@RequestParam @NotBlank @Size(max = 10000) String message) {
        producerTemplate.sendBody("direct:publish", message);
        return "Message published successfully.";
    }

    @GetMapping("/read")
    public String readFromQueue() {
        String message = producerTemplate.requestBody("direct:read", null, String.class);
        return "Message read from queue: " + message;
    }

    @GetMapping("/drain")
    public Object drainQueue() {
        return producerTemplate.requestBody("direct:drain", (Object) null, Object.class);
    }
}