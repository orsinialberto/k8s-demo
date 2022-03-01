package com.example.demo.controller;

import com.example.demo.core.model.CustomerResource;
import com.example.demo.core.service.CustomerService;
import com.example.demo.repository.model.Customer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static java.util.stream.Collectors.toList;

@RestController
@RequestMapping(value = "/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(final CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping
    public CustomerResource create(@RequestBody final JsonNode body) throws JsonProcessingException {

        final Customer customer = customerService.create(body);

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
    public void deleteAll() {

        customerService.deleteAll();
    }

    @DeleteMapping(value = "/{id}")
    public void deleteById(@PathVariable("id") final String id) {

        customerService.deleteById(id);
    }
}
