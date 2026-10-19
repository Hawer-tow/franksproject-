package com.example.MobileDb.entity.customer;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "Ugx2congorates")
public class Ugx2CongoRate {
    @Id
    private String ugx2congoratedate;

    private double ugx2congorate;
    private double min_transactionvalue;
    private double max_transactionValue;

    // getters and setters
    public String getUgx2congoratedate() { return ugx2congoratedate; }
    public void setUgx2congoratedate(String date) { this.ugx2congoratedate = date; }

    public double getUgx2congorate() { return ugx2congorate; }
    public void setUgx2congorate(double rate) { this.ugx2congorate = rate; }

    public double getMin_transactionvalue() { return min_transactionvalue; }
    public void setMin_transactionvalue(double min) { this.min_transactionvalue = min; }

    public double getMax_transactionValue() { return max_transactionValue; }
    public void setMax_transactionValue(double max) { this.max_transactionValue = max; }
}
