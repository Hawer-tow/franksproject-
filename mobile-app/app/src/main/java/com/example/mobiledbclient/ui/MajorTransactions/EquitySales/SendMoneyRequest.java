package com.example.mobiledbclient.ui.MajorTransactions.EquitySales;

public class SendMoneyRequest {
    private String senderAccount;
    private String recipientPhone;
    private String amount;
    private String telco;
    private String rate;

    public SendMoneyRequest(String senderAccount, String recipientPhone, String amount, String telco, String rate) {
        this.senderAccount = senderAccount;
        this.recipientPhone = recipientPhone;
        this.amount = amount;
        this.telco = telco;
        this.rate = rate;
    }

    // getters and setters
    public String getSenderAccount() { return senderAccount; }
    public void setSenderAccount(String senderAccount) { this.senderAccount = senderAccount; }

    public String getRecipientPhone() { return recipientPhone; }
    public void setRecipientPhone(String recipientPhone) { this.recipientPhone = recipientPhone; }

    public String getAmount() { return amount; }
    public void setAmount(String amount) { this.amount = amount; }

    public String getTelco() { return telco; }
    public void setTelco(String telco) { this.telco = telco; }

    public String getRate() { return rate; }
    public void setRate(String rate) { this.rate = rate; }
}
