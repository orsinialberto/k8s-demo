package com.example.demo.core.service;

import com.example.demo.repository.CustomerRepository;
import com.example.demo.repository.model.Customer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.example.demo.core.util.JsonUtil.convertToObject;

@Service
public class CustomerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomerService.class);

    private final CustomerRepository customerRepository;

    public CustomerService(final CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @KafkaListener(topics = "customer", groupId = "customer-create")
    public Customer create(final String message) {

        LOGGER.info("received a message {} from topic 'customer'", message);

        final Customer customer = customerRepository.save(convertToObject(message, Customer.class));

        LOGGER.info("created customer with id {}", customer.getId());

        return customer;
    }

    public List<Customer> findAll() {

        final List<Customer> customers = StreamSupport.stream(customerRepository.findAll().spliterator(), false)
                .collect(Collectors.toList());

        LOGGER.info("found {} customers", customers.size());

        return customers;
    }

    public Customer findById(final String id) {

        final Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("customer not found"));

        LOGGER.info("found customers {}", customer.getId());

        return customer;
    }

    public void deleteAll() {

        customerRepository.deleteAll();

        LOGGER.info("deleted all customers");
    }

    public void deleteById(final String id) {

        customerRepository.deleteById(id);

        LOGGER.info("deleted customers {}", id);
    }
}
