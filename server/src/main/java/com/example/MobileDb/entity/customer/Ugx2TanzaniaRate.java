package com.example.MobileDb.entity.customer;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "Ugx2tanzaniarates")
public class Ugx2TanzaniaRate {
    @Id
    private String ugx2tanzaniaratedate;

    private double ugx2tanzaniarate;
    private double min_transactionvalue;
    private double max_transactionValue;

    // Getters and setters
    public String getUgx2tanzaniaratedate() { return ugx2tanzaniaratedate; }
    public void setUgx2tanzaniaratedate(String date) { this.ugx2tanzaniaratedate = date; }

    public double getUgx2tanzaniarate() { return ugx2tanzaniarate; }
    public void setUgx2tanzaniarate(double rate) { this.ugx2tanzaniarate = rate; }

    public double getMin_transactionvalue() { return min_transactionvalue; }
    public void setMin_transactionvalue(double min) { this.min_transactionvalue = min; }

    public double getMax_transactionValue() { return max_transactionValue; }
    public void setMax_transactionValue(double max) { this.max_transactionValue = max; }
}



