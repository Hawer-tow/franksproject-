package com.example.MobileDb.controller.customer;

import com.example.MobileDb.entity.customer.Kenya2Ugxrates;
import com.example.MobileDb.repository.customer.Kenya2UgxratesRepository;
import org.springframework.http.ResponseEntity; 
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/kenya2ugx")
public class Kenya2UgxratesController {

    private final Kenya2UgxratesRepository repository;

    public Kenya2UgxratesController(Kenya2UgxratesRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<Kenya2Ugxrates> getAllRates() {
        return repository.findAll();
    }

    @PostMapping
    public Kenya2Ugxrates addRate(@RequestBody Kenya2Ugxrates rate) {
        return repository.save(rate);
    }

    @GetMapping("/{date}")
    public ResponseEntity<Kenya2Ugxrates> getRateByDate(@PathVariable String date) {
        return repository.findById(date)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
