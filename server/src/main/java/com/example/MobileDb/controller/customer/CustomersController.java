package com.example.MobileDb.controller.customer;

import com.example.MobileDb.entity.customer.Customers;
import com.example.MobileDb.entity.customer.CustomerDeletes;
import com.example.MobileDb.repository.customer.CustomersRepository;
import com.example.MobileDb.repository.customer.CustomerDeletesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/api/customers")
public class CustomersController {

    @Autowired
    private CustomersRepository customersRepository;

    @Autowired
    private CustomerDeletesRepository customerDeletesRepository;

    @GetMapping
    public List<Customers> getAllCustomers() {
        return customersRepository.findAll();
    }

    @GetMapping("/{id}")
    public Customers getCustomerById(@PathVariable int id) {
        return customersRepository.findById(id).orElse(null);
    }

    @PostMapping
    public Customers addCustomer(@RequestBody Customers customer) {
        return customersRepository.save(customer);
    }

    @PutMapping("/{id}")
    public Customers updateCustomer(@PathVariable int id, @RequestBody Customers customer) {
        customer.setCustomer_ID(id);
        return customersRepository.save(customer);
    }

    @DeleteMapping("/{id}")
    public void deleteCustomer(@PathVariable int id, @RequestParam String deletedBy) {
        Customers customer = customersRepository.findById(id).orElse(null);
        if (customer != null) {
            // Save to deletes table
            CustomerDeletes deleted = new CustomerDeletes(customer, deletedBy);
            customerDeletesRepository.save(deleted);

            // Delete from main table
            customersRepository.deleteById(id);
        }
    }

    @DeleteMapping
    public void clearAllCustomers() {
        customersRepository.deleteAll();
    }
}
