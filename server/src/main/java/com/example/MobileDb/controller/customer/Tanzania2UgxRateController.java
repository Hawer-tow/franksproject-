package com.example.MobileDb.controller.customer;

import com.example.MobileDb.entity.customer.Tanzania2UgxRate;
import com.example.MobileDb.repository.customer.Tanzania2UgxRateRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tanzania2ugx")
public class Tanzania2UgxRateController {

    private final Tanzania2UgxRateRepository repository;

    public Tanzania2UgxRateController(Tanzania2UgxRateRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<Tanzania2UgxRate> getAllRates() {
        return repository.findAll();
    }

    @GetMapping("/{date}")
    public ResponseEntity<Tanzania2UgxRate> getRateByDate(@PathVariable String date) {
        return repository.findById(date)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Tanzania2UgxRate addRate(@RequestBody Tanzania2UgxRate rate) {
        return repository.save(rate);
    }
}
