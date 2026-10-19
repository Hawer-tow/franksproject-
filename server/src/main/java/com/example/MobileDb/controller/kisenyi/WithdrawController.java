package com.example.MobileDb.controller.kisenyi;

import com.example.MobileDb.entity.kisenyi.Withdraw;
import com.example.MobileDb.repository.kisenyi.WithdrawRepository;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.dao.EmptyResultDataAccessException;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/withdraw")
public class WithdrawController {

    private final WithdrawRepository repository;

    public WithdrawController(WithdrawRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public ResponseEntity<List<Withdraw>> getAll() {
        List<Withdraw> list = repository.findAll();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{tid}")
    public ResponseEntity<Withdraw> getById(@PathVariable("tid") String tid) {
        Optional<Withdraw> opt = repository.findById(tid);
        return opt.map(ResponseEntity::ok)
                  .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PostMapping
    public ResponseEntity<Withdraw> createOrUpdate(@RequestBody Withdraw payload) {
        if (payload == null || payload.getTid() == null || payload.getTid().trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        Withdraw saved = repository.save(payload);
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
