package com.example.MobileDb.controller.kisenyi;

import com.example.MobileDb.entity.kisenyi.Commission;
import com.example.MobileDb.repository.kisenyi.CommissionRepository;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.dao.EmptyResultDataAccessException;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/commission")
public class CommissionController {

    private final CommissionRepository repository;

    public CommissionController(CommissionRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public ResponseEntity<List<Commission>> getAll() {
        List<Commission> list = repository.findAll();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{cid}")
    public ResponseEntity<Commission> getById(@PathVariable("cid") String cid) {
        Optional<Commission> opt = repository.findById(cid);
        return opt.map(ResponseEntity::ok)
                  .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PostMapping
    public ResponseEntity<Commission> create(@RequestBody Commission payload) {
        if (payload == null || payload.getCid() == null || payload.getCid().trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        Commission saved = repository.save(payload);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{cid}")
    public ResponseEntity<Commission> update(@PathVariable("cid") String cid,
                                             @RequestBody Commission payload) {
        if (payload == null) return ResponseEntity.badRequest().build();
        if (payload.getCid() == null) payload.setCid(cid);
        if (!cid.equals(payload.getCid())) return ResponseEntity.badRequest().build();

        Optional<Commission> existing = repository.findById(cid);
        if (!existing.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        Commission toSave = payload;
        Commission saved = repository.save(toSave);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{cid}")
    public ResponseEntity<Void> delete(@PathVariable("cid") String cid) {
        try {
            repository.deleteById(cid);
            return ResponseEntity.noContent().build();
        } catch (EmptyResultDataAccessException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
