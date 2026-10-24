package com.camel.oss.router;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class RouteA extends RouteBuilder {
    @Override
    public void configure() throws Exception {
        from("direct:Subscribe")
        .routeId("RouteSubscribe")
        .log("Subscribed message: ${body}")
        .to("log:Subscribe");
    }
}
