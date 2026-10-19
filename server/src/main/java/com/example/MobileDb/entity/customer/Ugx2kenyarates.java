package com.example.MobileDb.entity.customer;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "Ugx2kenyarates")
public class Ugx2kenyarates {
    @Id
    private String ugx2kenyaratedate;

    private double ugx2kenyarate;
    private double min_transactionvalue;
    private double max_transactionValue;

    // getters and setters
    public String getUgx2kenyaratedate() { return ugx2kenyaratedate; }
    public void setUgx2kenyaratedate(String date) { this.ugx2kenyaratedate = date; }

    public double getUgx2kenyarate() { return ugx2kenyarate; }
    public void setUgx2kenyarate(double rate) { this.ugx2kenyarate = rate; }

    public double getMin_transactionvalue() { return min_transactionvalue; }
    public void setMin_transactionvalue(double min) { this.min_transactionvalue = min; }

    public double getMax_transactionValue() { return max_transactionValue; }
    public void setMax_transactionValue(double max) { this.max_transactionValue = max; }
}
