package com.example.MobileDb.entity.customer;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "Kenya2Ugxrates")
public class Kenya2Ugxrates {
    @Id
    private String kenya2Ugxratedate;

    private double kenya2Ugxrate;
    private double min_transactionvalue;
    private double max_transactionValue;

    // getters and setters
    public String getKenya2Ugxratedate() { return kenya2Ugxratedate; }
    public void setKenya2Ugxratedate(String date) { this.kenya2Ugxratedate = date; }

    public double getKenya2Ugxrate() { return kenya2Ugxrate; }
    public void setKenya2Ugxrate(double rate) { this.kenya2Ugxrate = rate; }

    public double getMin_transactionvalue() { return min_transactionvalue; }
    public void setMin_transactionvalue(double min) { this.min_transactionvalue = min; }

    public double getMax_transactionValue() { return max_transactionValue; }
    public void setMax_transactionValue(double max) { this.max_transactionValue = max; }
}
