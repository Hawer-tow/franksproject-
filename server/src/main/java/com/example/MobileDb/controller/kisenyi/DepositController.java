package com.example.MobileDb.controller.kisenyi;

import com.example.MobileDb.entity.kisenyi.Deposit;
import com.example.MobileDb.repository.kisenyi.DepositRepository;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.dao.EmptyResultDataAccessException;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/deposit")
public class DepositController {

    private final DepositRepository repository;

    public DepositController(DepositRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public ResponseEntity<List<Deposit>> getAll() {
        List<Deposit> list = repository.findAll();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{tid}")
    public ResponseEntity<Deposit> getById(@PathVariable("tid") String tid) {
        Optional<Deposit> opt = repository.findById(tid);
        return opt.map(ResponseEntity::ok)
                  .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PostMapping
    public ResponseEntity<Deposit> createOrUpdate(@RequestBody Deposit payload) {
        if (payload == null || payload.getTid() == null || payload.getTid().trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        Deposit saved = repository.save(payload);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{tid}")
    public ResponseEntity<Void> delete(@PathVariable("tid") String tid) {
        try {
            repository.deleteById(tid);
            return ResponseEntity.noContent().build();
        } catch (EmptyResultDataAccessException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
