package com.example.demo.core.service;

import com.example.demo.repository.CustomerRepository;
import com.example.demo.repository.model.Customer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class CustomerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomerService.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    final CustomerRepository customerRepository;

    public CustomerService(final CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public Customer create(final JsonNode baseResource) throws JsonProcessingException {

          final String base = OBJECT_MAPPER.writeValueAsString(baseResource);
          final Customer customer = new Customer(base);

          final Customer customerSaved = customerRepository.save(customer);

          LOGGER.info("created customer with id {}", customer.getId());

          return customerSaved;

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
