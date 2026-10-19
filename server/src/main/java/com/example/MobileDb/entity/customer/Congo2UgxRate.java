package com.example.MobileDb.entity.customer;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "congo2Ugxrates")
public class Congo2UgxRate {
    @Id
    private String congo2Ugxratedate;

    private double congo2Ugxrate;
    private double min_transactionvalue;
    private double max_transactionValue;

    // getters and setters
    public String getCongo2Ugxratedate() { return congo2Ugxratedate; }
    public void setCongo2Ugxratedate(String date) { this.congo2Ugxratedate = date; }

    public double getCongo2Ugxrate() { return congo2Ugxrate; }
    public void setCongo2Ugxrate(double rate) { this.congo2Ugxrate = rate; }

    public double getMin_transactionvalue() { return min_transactionvalue; }
    public void setMin_transactionvalue(double min) { this.min_transactionvalue = min; }

    public double getMax_transactionValue() { return max_transactionValue; }
    public void setMax_transactionValue(double max) { this.max_transactionValue = max; }
}

