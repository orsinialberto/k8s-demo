package com.example.demo.backend.activemq;

import com.example.demo.backend.elasticsearch.ElasticsearchClient;
import com.example.demo.repository.CustomerRepository;
import org.apache.camel.Exchange;
import org.apache.camel.Handler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;

import static java.util.Objects.requireNonNull;

@Service
public class CleanerProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(CleanerProcessor.class);

    private final CustomerRepository customerRepository;
    private final ElasticsearchClient elasticsearchClient;

    public CleanerProcessor(
            final CustomerRepository customerRepository,
            final ElasticsearchClient elasticsearchClient
    ) {
        this.customerRepository = customerRepository;
        this.elasticsearchClient = elasticsearchClient;
    }

    @Handler
    public void process(final Exchange message) throws IOException {

        requireNonNull(message, "message must not be null");

        LOGGER.info("deleting all customers");
        customerRepository.deleteAll();
        LOGGER.info("deleted all customers");

        LOGGER.info("deleting index");
        elasticsearchClient.deleteIndex();
        LOGGER.info("deleted index");

        LOGGER.info("creating index");
        elasticsearchClient.createIndex();
        LOGGER.info("created index");
    }
}
