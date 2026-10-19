package com.example.mobiledbclient.ui.Kisenyi.expenses;

import com.google.gson.annotations.SerializedName;

public class Expense {
    private double amount;
    private String date;
    private String description;

    @SerializedName("id")
    private Integer id; // <-- no transient

    public Expense() {}

    public Expense(double amount, String date, String description) {
        this.amount = amount;
        this.date = date;
        this.description = description;
    }

    public Expense(Integer serverId, double amount, String date, String description) {
        this.id = serverId;
        this.amount = amount;
        this.date = date;
        this.description = description;
    }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
}
