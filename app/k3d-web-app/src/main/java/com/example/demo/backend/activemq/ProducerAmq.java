package com.example.demo.backend.activemq;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.camel.ProducerTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

import static com.example.demo.core.util.JsonUtil.OBJECT_MAPPER;

@Service
public class ProducerAmq {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProducerAmq.class);

    private final ProducerTemplate producerTemplate;

    public ProducerAmq(final ProducerTemplate producerTemplate) {
        this.producerTemplate = producerTemplate;
    }

    public void sendMessageToQueue(final String endpointUri, final Object message) throws JsonProcessingException {

        final Map<String, Object> map = new HashMap<>();

        LOGGER.info("sending a {} message to {}", message, endpointUri);
        producerTemplate.sendBodyAndHeaders(endpointUri, OBJECT_MAPPER.writeValueAsString(message), map);
        LOGGER.info("sent a {} message to {}", message, endpointUri);
    }
}
