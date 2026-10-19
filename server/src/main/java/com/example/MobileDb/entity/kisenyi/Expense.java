package com.example.MobileDb.entity.kisenyi;


import jakarta.persistence.*;
import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonFormat;


@Entity
@Table(name = "expenses")
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "expences_ID")   // must match DB spelling
    private int id;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Column(name = "expences_date")
    private LocalDate date;

    @Column(name = "expense_amount")
    private double amount;

    @Column(name = "expense_Description") // capital D to match DB
    private String description;

    public Expense() {}

    public Expense(int id, LocalDate date, double amount, String description) {
        this.id = id;
        this.date = date;
        this.amount = amount;
        this.description = description;
    }



    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}



//netstat -ano | findstr :8080
//taskkill /PID (thestring) /F
//.\gradlew clean build
//.\gradlew bootRun

