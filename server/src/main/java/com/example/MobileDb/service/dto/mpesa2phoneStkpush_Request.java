package com.example.MobileDb.service.dto;



public class mpesa2phoneStkpush_Request {
    private String phoneNumber;
    private double amount;
    private double rate;

    // getters and setters
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public double getRate() { return rate; }
    public void setRate(double rate) { this.rate = rate; }
}
