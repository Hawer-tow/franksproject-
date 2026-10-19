package com.example.mobiledbclient.ui.MajorTransactions.EquitySales;

public class EquityAccountSendMoneyRequest {
    private String recipientAccount; // Matches server's recipientAccount
    private String senderAccount;    // Matches server's senderAccount
    private String amount;
    private String rate;
    private String telco;            // Set to "BANK" for this fragment

    // Getters and Setters
    public String getRecipientAccount() { return recipientAccount; }
    public void setRecipientAccount(String recipientAccount) { this.recipientAccount = recipientAccount; }

    public String getSenderAccount() { return senderAccount; }
    public void setSenderAccount(String senderAccount) { this.senderAccount = senderAccount; }

    public String getAmount() { return amount; }
    public void setAmount(String amount) { this.amount = amount; }

    public String getRate() { return rate; }
    public void setRate(String rate) { this.rate = rate; }

    public String getTelco() { return telco; }
    public void setTelco(String telco) { this.telco = telco; }
}
