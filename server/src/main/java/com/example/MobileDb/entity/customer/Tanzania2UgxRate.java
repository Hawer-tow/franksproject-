package com.example.MobileDb.entity.customer;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "tanzania2Ugxrates")
public class Tanzania2UgxRate {
    @Id
    private String tanzania2Ugxratedate;

    private double tanzania2Ugxrate;
    private double min_transactionvalue;
    private double max_transactionValue;

    // Getters and setters
    public String getTanzania2Ugxratedate() { return tanzania2Ugxratedate; }
    public void setTanzania2Ugxratedate(String date) { this.tanzania2Ugxratedate = date; }

    public double getTanzania2Ugxrate() { return tanzania2Ugxrate; }
    public void setTanzania2Ugxrate(double rate) { this.tanzania2Ugxrate = rate; }

    public double getMin_transactionvalue() { return min_transactionvalue; }
    public void setMin_transactionvalue(double min) { this.min_transactionvalue = min; }

    public double getMax_transactionValue() { return max_transactionValue; }
    public void setMax_transactionValue(double max) { this.max_transactionValue = max; }
}
