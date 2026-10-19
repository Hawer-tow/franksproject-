package com.example.MobileDb.controller.customer;

import com.example.MobileDb.entity.customer.Ugx2RwandaRate;
import com.example.MobileDb.repository.customer.Ugx2RwandaRateRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ugx2rwanda")
public class Ugx2RwandaRateController {

    private final Ugx2RwandaRateRepository repository;

    public Ugx2RwandaRateController(Ugx2RwandaRateRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<Ugx2RwandaRate> getAllRates() {
        return repository.findAll();
    }

    @GetMapping("/{date}")
    public ResponseEntity<Ugx2RwandaRate> getRateByDate(@PathVariable String date) {
        return repository.findById(date)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Ugx2RwandaRate addRate(@RequestBody Ugx2RwandaRate rate) {
        return repository.save(rate);
    }
}
