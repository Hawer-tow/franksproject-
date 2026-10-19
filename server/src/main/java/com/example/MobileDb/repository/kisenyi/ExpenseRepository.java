package com.example.MobileDb.repository.kisenyi;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.MobileDb.entity.kisenyi.Expense;

import java.time.LocalDate;
import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Integer> {
  
	 // Derived query: find all expenses between two dates
    List<Expense> findByDateBetween(LocalDate start, LocalDate end);
	
}





