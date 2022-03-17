package com.example.demo.backend.kafka;

import com.example.demo.core.service.CustomerService;
import com.example.demo.repository.model.Customer;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class Consumer {

    private final CustomerService customerService;

    public Consumer(final CustomerService customerService) {
        this.customerService = customerService;
    }

    @KafkaListener(topics = "customer", groupId = "customer-create", containerFactory = "kafkaListenerContainerFactory")
    public void consume(@Payload final List<Customer> message) {
        customerService.create(message);
    }
}
