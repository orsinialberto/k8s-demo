package com.example.demo.backend.activemq;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Service;

@Service
public class CleanerRoute extends RouteBuilder {

    public static final String ROUTE_ID = "cleaner";

    private final CleanerProcessor cleanerProcessor;

    public CleanerRoute(final CleanerProcessor cleanerProcessor) {
        this.cleanerProcessor = cleanerProcessor;
    }

    @Override
    public void configure() {
        from("activemq:cleaner")
                .routeId(ROUTE_ID)
                .autoStartup(true)
                .bean(cleanerProcessor);
    }
}
