package com.example.MobileDb.controller.kisenyi;

import com.example.MobileDb.entity.kisenyi.Expense;
import com.example.MobileDb.repository.kisenyi.ExpenseRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate; 
import java.util.List;



@RestController
@RequestMapping("/expenses")
public class ExpenseController {

    private final ExpenseRepository repo;

    public ExpenseController(ExpenseRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<Expense> getAllExpenses() {
        return repo.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Expense> getExpenseById(@PathVariable("id") Integer id) {
        return repo.findById(id)
                   .map(ResponseEntity::ok)
                   .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Expense addExpense(@RequestBody Expense expense) {
        return repo.save(expense);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Expense> updateExpense(@PathVariable("id") Integer id, @RequestBody Expense expense) {
        return repo.findById(id)
                   .map(existing -> {
                       // copy fields
                       existing.setDate(expense.getDate());
                       existing.setAmount(expense.getAmount());
                       existing.setDescription(expense.getDescription());
                       Expense updated = repo.save(existing);
                       return ResponseEntity.ok(updated);
                   })
                   .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExpense(@PathVariable("id") Integer id) {
        if (!repo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
    
    
    
    // New endpoint: query by range
    @GetMapping("/range")
    public List<Expense> getExpensesInRange(@RequestParam("days") int days) {
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(days - 1); // inclusive range
        return repo.findByDateBetween(start, end);
    }

    // Optional: monthly
    @GetMapping("/monthly")
    public List<Expense> getMonthlyExpenses() {
        LocalDate end = LocalDate.now();
        LocalDate start = end.withDayOfMonth(1); // first day of month
        return repo.findByDateBetween(start, end);
    }
}







