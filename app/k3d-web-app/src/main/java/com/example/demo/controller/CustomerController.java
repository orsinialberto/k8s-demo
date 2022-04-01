package com.example.demo.controller;

import com.example.demo.backend.activemq.ProducerAmq;
import com.example.demo.backend.kafka.ProducerKafka;
import com.example.demo.core.model.CustomerResource;
import com.example.demo.core.service.CustomerService;
import com.example.demo.repository.model.Customer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.example.demo.core.util.JsonUtil.convertToString;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

@RestController
@RequestMapping(value = "/customers")
public class CustomerController {

    private final CustomerService customerService;
    private final ProducerKafka producerKafka;
    private final ProducerAmq producerAmq;

    public CustomerController(
            final CustomerService customerService,
            final ProducerKafka producerKafka,
            final ProducerAmq producerAmq
    ) {
        this.customerService = customerService;
        this.producerKafka = producerKafka;
        this.producerAmq = producerAmq;
    }

    @PostMapping
    public CustomerResource create(@RequestBody final JsonNode body) {

        requireNonNull(body, "body must not be null");

        final String base = convertToString(body);
        final Customer customer = new Customer(base);

        producerKafka.sendMessageToTopic(customer, "customer");

        return customer.toResource();
    }

    @GetMapping
    public List<CustomerResource> findAll() {

        return customerService.findAll()
                .stream()
                .map(Customer::toResource)
                .collect(toList());
    }

    @GetMapping(value = "/{id}")
    public CustomerResource findById(@PathVariable("id") final String id) {

        return customerService.findById(id).toResource();
    }

    @DeleteMapping
    public void deleteAll() throws JsonProcessingException {

        producerAmq.sendMessageToQueue("activemq:cleaner", "DELETE_ALL");
    }

    @DeleteMapping(value = "/{id}")
    public void deleteById(@PathVariable("id") final String id) {

        customerService.deleteById(id);
    }
}
