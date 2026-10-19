package com.example.MobileDb.controller.customer;


import com.example.MobileDb.entity.customer.Congo2UgxRate;
import org.springframework.http.ResponseEntity; 
import com.example.MobileDb.repository.customer.Congo2UgxRateRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/congo2ugx")
public class Congo2UgxRateController {

    private final Congo2UgxRateRepository repository;

    public Congo2UgxRateController(Congo2UgxRateRepository repository) {
        this.repository = repository;
    }

 
    @GetMapping
    public List<Congo2UgxRate> getAllRates() {
        return repository.findAll();
    }

    @GetMapping("/{date}")
    public ResponseEntity<Congo2UgxRate> getRateByDate(@PathVariable String date) {
        return repository.findById(date)
                         .map(ResponseEntity::ok)
                         .orElse(ResponseEntity.notFound().build());
    }


    @PostMapping
    public Congo2UgxRate addRate(@RequestBody Congo2UgxRate rate) {
        return repository.save(rate);
    }
}
