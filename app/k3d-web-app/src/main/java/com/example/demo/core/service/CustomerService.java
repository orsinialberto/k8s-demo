package com.example.demo.core.service;

import com.example.demo.backend.elasticsearch.ElasticsearchClient;
import com.example.demo.core.model.Action;
import com.example.demo.repository.CustomerRepository;
import com.example.demo.repository.model.Customer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.example.demo.core.model.Action.INDEX;
import static com.example.demo.core.util.JsonUtil.OBJECT_MAPPER;

@Service
public class CustomerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomerService.class);

    private final CustomerRepository customerRepository;
    private final ElasticsearchClient elasticsearchClient;

    public CustomerService(final CustomerRepository customerRepository, final ElasticsearchClient elasticsearchClient) {
        this.customerRepository = customerRepository;
        this.elasticsearchClient = elasticsearchClient;
    }

    public void create(final List<Customer> customers) {

        LOGGER.info("saving {} customers", customers.size());

        final Iterable<Customer> iterable = customerRepository.saveAll(customers);

        LOGGER.info("saved {} customers", ((Collection<?>) iterable).size());

        final String body = StreamSupport.stream(iterable.spliterator(), true)
                .map(customer -> convertCustomer(customer, INDEX))
                .collect(Collectors.joining());

        try {
            LOGGER.info("indexing {} customers", ((Collection<?>) iterable).size());

            elasticsearchClient.index(body);

            LOGGER.info("indexed {} customers", ((Collection<?>) iterable).size());
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    private String convertCustomer(final Customer customer, final Action action) {

        final ObjectNode actionNode = OBJECT_MAPPER.createObjectNode();

        final ObjectNode actionProperties = actionNode.putObject(action.toString().toLowerCase());
        actionProperties.put("_index", "customer");
        actionProperties.put("_id", customer.getId());

        final JsonNode sourceNode = OBJECT_MAPPER.valueToTree(customer);

        return action.equals(INDEX) ? actionNode + "\n" + sourceNode + "\n" : actionNode + "\n";
    }
}
