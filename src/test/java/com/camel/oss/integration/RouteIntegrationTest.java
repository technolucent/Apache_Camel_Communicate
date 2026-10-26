package com.camel.oss.integration;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.apache.camel.ProducerTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
class RouteIntegrationTest {

    @Container
    static RabbitMQContainer rabbitMQ = new RabbitMQContainer("rabbitmq:3.13-management")
            .withExposedPorts(5672);

    @Autowired
    private ProducerTemplate producerTemplate;

    @DynamicPropertySource
    static void rabbitProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.rabbitmq.host", rabbitMQ::getHost);
        registry.add("spring.rabbitmq.port", rabbitMQ::getFirstMappedPort);
    }

    @Test
    void testPublishRoute() {
        String testMessage = "test-message-" + System.currentTimeMillis();
        String result = producerTemplate.requestBody("direct:publish", testMessage, String.class);
        assertNotNull(result);
    }

    @Test
    void testReadRoute() {
        String result = producerTemplate.requestBody("direct:read", null, String.class);
        assertNotNull(result);
    }

    @Test
    void testDrainRoute() {
        Object result = producerTemplate.requestBody("direct:drain", null, Object.class);
        assertNotNull(result);
    }

    @Test
    void testSubscribeRoute() {
        String testMessage = "subscribe-test-" + System.currentTimeMillis();
        String result = producerTemplate.requestBody("direct:Subscribe", testMessage, String.class);
        assertNotNull(result);
    }
}