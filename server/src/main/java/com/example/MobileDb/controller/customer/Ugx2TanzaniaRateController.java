package com.example.MobileDb.controller.customer;

import com.example.MobileDb.entity.customer.Ugx2TanzaniaRate;
import com.example.MobileDb.repository.customer.Ugx2TanzaniaRateRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ugx2tanzania")
public class Ugx2TanzaniaRateController {

    private final Ugx2TanzaniaRateRepository repository;

    public Ugx2TanzaniaRateController(Ugx2TanzaniaRateRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<Ugx2TanzaniaRate> getAllRates() {
        return repository.findAll();
    }

    @GetMapping("/{date}")
    public ResponseEntity<Ugx2TanzaniaRate> getRateByDate(@PathVariable String date) {
        return repository.findById(date)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Ugx2TanzaniaRate addRate(@RequestBody Ugx2TanzaniaRate rate) {
        return repository.save(rate);
    }
}
