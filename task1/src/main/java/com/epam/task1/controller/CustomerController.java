package com.epam.task1.controller;

import com.epam.task1.entity.CustomerModel;
import com.epam.task1.service.CustomerService;
import io.micrometer.core.instrument.MeterRegistry;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class CustomerController {

    private final CustomerService customerService;
    private final MeterRegistry meterRegistry;

    public CustomerController(CustomerService customerService, MeterRegistry meterRegistry) {
        this.customerService = customerService;
        this.meterRegistry = meterRegistry;
    }

    @PostMapping(path = "/customer",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Long> createNewCustomer(@RequestBody CustomerModel model) throws BadRequestException {
        Long id = customerService.create(model);
        meterRegistry.counter("customer.metrics", "operation", "created").increment();
        return ResponseEntity.ok().body(id);
    }

    @GetMapping(path = "/customer/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<CustomerModel> getCustomer(@PathVariable Long id) {
        meterRegistry.counter("customer.metrics", "operation", "extracted").increment();
        return ResponseEntity.ok().body(customerService.get(id));
    }

    @PutMapping(path = "/customer",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<CustomerModel> updateCustomer(@RequestBody CustomerModel model) {
        meterRegistry.counter("customer.metrics", "operation", "updated").increment();
        return ResponseEntity.ok().body(customerService.update(model));
    }

    @DeleteMapping("/customer/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) {
        customerService.delete(id);
        meterRegistry.counter("customer.metrics", "operation", "deleted").increment();
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
