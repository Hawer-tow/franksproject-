package com.example.MobileDb.entity.customer;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "rwanda2Ugxrates")
public class Rwanda2UgxRate {
    @Id
    @Column(name = "rwanda2Ugxratedate")
    @JsonProperty("rwanda2Ugxratedate")
    private String rwanda2Ugxratedate;

    @Column(name = "rwanda2Ugxrate")
    @JsonProperty("rwanda2Ugxrate")
    private double rwanda2Ugxrate;

    @Column(name = "min_transactionvalue")
    private double min_transactionvalue;

    @Column(name = "max_transactionValue")
    private double max_transactionValue;

    // Getters and setters
    public String getRwanda2Ugxratedate() { return rwanda2Ugxratedate; }
    public void setRwanda2Ugxratedate(String date) { this.rwanda2Ugxratedate = date; }

    public double getRwanda2Ugxrate() { return rwanda2Ugxrate; }
    public void setRwanda2Ugxrate(double rate) { this.rwanda2Ugxrate = rate; }

    public double getMin_transactionvalue() { return min_transactionvalue; }
    public void setMin_transactionvalue(double min) { this.min_transactionvalue = min; }

    public double getMax_transactionValue() { return max_transactionValue; }
    public void setMax_transactionValue(double max) { this.max_transactionValue = max; }
}
