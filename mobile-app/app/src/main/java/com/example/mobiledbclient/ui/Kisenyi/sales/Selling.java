package com.example.mobiledbclient.ui.Kisenyi.sales;

public class Selling {
    private Integer sellingId;
    private String sellingDate;       // ISO date string
    private Double sellingAmount;
    private Double sellingRate;
    private String receivingCountry;  // default "Kenya", but editable
    private String phoneNo;
    private String recipientName;
    private String sellDescription;
    private String status;
    private String transactionId;

    // --- New field ---
    private String saleType; // USER_INITIATED_BANK, USER_INITIATED_MPESA, BANK, MPESA

    // --- Hidden system fields (not shown/edited in UI unless user-initiated bank) ---
    private String senderAccount;
    private String receiverAccount;
    private String bankCode;
    private String currency;

    // --- Getters and Setters ---
    public Integer getSellingId() { return sellingId; }
    public void setSellingId(Integer sellingId) { this.sellingId = sellingId; }

    public String getSellingDate() { return sellingDate; }
    public void setSellingDate(String sellingDate) { this.sellingDate = sellingDate; }

    public Double getSellingAmount() { return sellingAmount; }
    public void setSellingAmount(Double sellingAmount) { this.sellingAmount = sellingAmount; }

    public Double getSellingRate() { return sellingRate; }
    public void setSellingRate(Double sellingRate) { this.sellingRate = sellingRate; }

    public String getReceivingCountry() { return receivingCountry; }
    public void setReceivingCountry(String receivingCountry) { this.receivingCountry = receivingCountry; }

    public String getPhoneNo() { return phoneNo; }
    public void setPhoneNo(String phoneNo) { this.phoneNo = phoneNo; }

    public String getRecipientName() { return recipientName; }
    public void setRecipientName(String recipientName) { this.recipientName = recipientName; }

    public String getSellDescription() { return sellDescription; }
    public void setSellDescription(String sellDescription) { this.sellDescription = sellDescription; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public String getSaleType() { return saleType; }
    public void setSaleType(String saleType) { this.saleType = saleType; }

    // Hidden system fields
    public String getSenderAccount() { return senderAccount; }
    public void setSenderAccount(String senderAccount) { this.senderAccount = senderAccount; }

    public String getReceiverAccount() { return receiverAccount; }
    public void setReceiverAccount(String receiverAccount) { this.receiverAccount = receiverAccount; }

    public String getBankCode() { return bankCode; }
    public void setBankCode(String bankCode) { this.bankCode = bankCode; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
}
