package com.example.MobileDb.controller.customer;

import com.example.MobileDb.entity.customer.Ugx2kenyarates; 
import com.example.MobileDb.repository.customer.Ugx2kenyaratesRepository;
import org.springframework.http.ResponseEntity; 
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ugx2kenya")
public class Ugx2kenyaratesController {

    private final Ugx2kenyaratesRepository repository;

    public Ugx2kenyaratesController(Ugx2kenyaratesRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<Ugx2kenyarates> getAllRates() {
        return repository.findAll();
    }

    @PostMapping
    public Ugx2kenyarates addRate(@RequestBody Ugx2kenyarates rate) {
        return repository.save(rate);
    }

    @GetMapping("/{date}")
    public ResponseEntity<Ugx2kenyarates> getRateByDate(@PathVariable String date) {
        return repository.findById(date)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}

