package com.example.MobileDb.controller.customer;



import com.example.MobileDb.entity.customer.Ugx2CongoRate;
//import com.example.MobileDb.entity.customer.Ugx2kenyarates;
import com.example.MobileDb.repository.customer.Ugx2CongoRateRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ugx2congo")
public class Ugx2CongoRateController {

    private final Ugx2CongoRateRepository repository;

    public Ugx2CongoRateController(Ugx2CongoRateRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<Ugx2CongoRate> getAllRates() {
        return repository.findAll();
    }

    
    @GetMapping("/{date}")
    public ResponseEntity<Ugx2CongoRate> getRateByDate(@PathVariable String date) {
        return repository.findById(date)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Ugx2CongoRate addRate(@RequestBody Ugx2CongoRate rate) {
        return repository.save(rate);
    }
}

