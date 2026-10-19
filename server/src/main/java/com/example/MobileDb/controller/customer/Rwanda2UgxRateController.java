package com.example.MobileDb.controller.customer;

import com.example.MobileDb.entity.customer.Rwanda2UgxRate;
import com.example.MobileDb.repository.customer.Rwanda2UgxRateRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rwanda2ugx")
public class Rwanda2UgxRateController {

    private final Rwanda2UgxRateRepository repository;

    public Rwanda2UgxRateController(Rwanda2UgxRateRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<Rwanda2UgxRate> getAllRates() {
        return repository.findAll();
    }

    @GetMapping("/{date}")
    public ResponseEntity<Rwanda2UgxRate> getRateByDate(@PathVariable String date) {
        return repository.findById(date)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Rwanda2UgxRate addRate(@RequestBody Rwanda2UgxRate rate) {
        return repository.save(rate);
    }
}
