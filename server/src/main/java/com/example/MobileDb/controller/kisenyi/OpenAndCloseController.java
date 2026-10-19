package com.example.MobileDb.controller.kisenyi;

import com.example.MobileDb.entity.kisenyi.OpenAndClose;
import com.example.MobileDb.repository.kisenyi.OpenAndCloseRepository;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/open_and_close")
public class OpenAndCloseController {

    private final OpenAndCloseRepository repo;

    public OpenAndCloseController(OpenAndCloseRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<OpenAndClose> getAll() {
        return repo.findAll();
    }

    @GetMapping("/range/{days}")
    public List<OpenAndClose> getInRange(@PathVariable int days) {
        return repo.findInRange(days);
    }

    @GetMapping("/monthly")
    public List<OpenAndClose> getMonthly() {
        return repo.findMonthly();
    }

    @GetMapping("/{date}")
    public ResponseEntity<OpenAndClose> getByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return repo.findById(date).map(ResponseEntity::ok)
                   .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public OpenAndClose add(@RequestBody OpenAndClose record) {
        return repo.save(record);
    }

    @PutMapping("/{date}")
    public ResponseEntity<OpenAndClose> update(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestBody OpenAndClose record) {
        return repo.findById(date).map(existing -> {
            existing.setUgandaOpening(record.getUgandaOpening());
            existing.setKenyaOpening(record.getKenyaOpening());
            existing.setUgandaCashIn(record.getUgandaCashIn());
            existing.setKenyaCashIn(record.getKenyaCashIn());
            existing.setUgandaClosing(record.getUgandaClosing());
            existing.setKenyaClosing(record.getKenyaClosing());
            existing.setDescription(record.getDescription());
            return ResponseEntity.ok(repo.save(existing));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{date}")
    public ResponseEntity<Void> delete(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        if (!repo.existsById(date)) return ResponseEntity.notFound().build();
        repo.deleteById(date);
        return ResponseEntity.noContent().build();
    }
}
