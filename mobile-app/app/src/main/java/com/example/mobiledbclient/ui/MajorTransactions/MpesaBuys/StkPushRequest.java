package com.example.mobiledbclient.ui.MajorTransactions.MpesaBuys;



public class StkPushRequest {
    private String phoneNumber;
    private double amount;
    private double rate;

    public StkPushRequest(String phoneNumber, double amount, double rate) {
        this.phoneNumber = phoneNumber;
        this.amount = amount;
        this.rate = rate;
    }

    // getters and setters
}

