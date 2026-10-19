package com.example.mobiledbclient.ui.MajorTransactions.EquityBuys;

public class StkPushRequest {
    private String phoneNumber;
    private String amount;
    private String rate;
    private String telco;

    // getters and setters
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getAmount() { return amount; }
    public void setAmount(String amount) { this.amount = amount; }

    public String getRate() { return rate; }
    public void setRate(String rate) { this.rate = rate; }


    public String getTelco() { return telco; }
    public void setTelco(String telco) { this.telco = telco; }

}
