package com.example.demo.backend.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import static com.example.demo.core.util.JsonUtil.convertToString;
import static java.util.Objects.requireNonNull;

@Service
public class Producer {

    private final KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    public Producer(final KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendMessageToTopic(final Object message, final String topic) {

        requireNonNull(topic, "topic must not be null");
        requireNonNull(message, "message must not be null");

        this.kafkaTemplate.send(topic, convertToString(message));
    }
}
