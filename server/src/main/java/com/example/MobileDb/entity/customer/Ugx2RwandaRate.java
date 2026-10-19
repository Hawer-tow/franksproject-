package com.example.MobileDb.entity.customer;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "Ugx2rwandarates")
public class Ugx2RwandaRate {
    @Id
    @Column(name = "Ugx2rwandaratedate")
    @JsonProperty("ugx2rwandaratedate")
    private String ugx2rwandaratedate;

    @Column(name = "Ugx2rwandarate")
    @JsonProperty("ugx2rwandarate")
    private double ugx2rwandarate;

    @Column(name = "min_transactionvalue")
    private double min_transactionvalue;

    @Column(name = "max_transactionValue")
    private double max_transactionValue;

    // Getters and setters
    public String getUgx2rwandaratedate() { return ugx2rwandaratedate; }
    public void setUgx2rwandaratedate(String date) { this.ugx2rwandaratedate = date; }

    public double getUgx2rwandarate() { return ugx2rwandarate; }
    public void setUgx2rwandarate(double rate) { this.ugx2rwandarate = rate; }

    public double getMin_transactionvalue() { return min_transactionvalue; }
    public void setMin_transactionvalue(double min) { this.min_transactionvalue = min; }

    public double getMax_transactionValue() { return max_transactionValue; }
    public void setMax_transactionValue(double max) { this.max_transactionValue = max; }
}
