package com.example.mobiledbclient.ui.Kisenyi.sales;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.Ignore;

@Entity(tableName = "selling")
public class SellingEntity {
    @PrimaryKey(autoGenerate = true)
    private int localId;

    private Integer serverId;
    private String sellingDate;
    private double sellingAmount;
    private double sellingRate;

    // Default "Kenya" when created, but editable if user-initiated
    private String receivingCountry;

    private String phoneNo;
    private String recipientName;
    private String sellDescription;

    // --- Bank/M-PESA related fields ---
    private String senderAccount;
    private String receiverAccount;
    private String bankCode;
    private String transactionId;
    private String currency;

    private String status;

    // --- New field for provenance ---
    // USER_INITIATED_BANK, USER_INITIATED_MPESA, BANK, MPESA
    private String saleType;

    private boolean synced;
    private boolean markedForDelete;

    @Ignore
    private boolean editing; // UI-only

    // --- Getters and Setters ---
    public int getLocalId() { return localId; }
    public void setLocalId(int localId) { this.localId = localId; }

    public Integer getServerId() { return serverId; }
    public void setServerId(Integer serverId) { this.serverId = serverId; }

    public String getSellingDate() { return sellingDate; }
    public void setSellingDate(String sellingDate) { this.sellingDate = sellingDate; }

    public double getSellingAmount() { return sellingAmount; }
    public void setSellingAmount(double sellingAmount) { this.sellingAmount = sellingAmount; }

    public double getSellingRate() { return sellingRate; }
    public void setSellingRate(double sellingRate) { this.sellingRate = sellingRate; }

    public String getReceivingCountry() { return receivingCountry; }
    public void setReceivingCountry(String receivingCountry) { this.receivingCountry = receivingCountry; }

    public String getPhoneNo() { return phoneNo; }
    public void setPhoneNo(String phoneNo) { this.phoneNo = phoneNo; }

    public String getRecipientName() { return recipientName; }
    public void setRecipientName(String recipientName) { this.recipientName = recipientName; }

    public String getSellDescription() { return sellDescription; }
    public void setSellDescription(String sellDescription) { this.sellDescription = sellDescription; }

    public String getSenderAccount() { return senderAccount; }
    public void setSenderAccount(String senderAccount) { this.senderAccount = senderAccount; }

    public String getReceiverAccount() { return receiverAccount; }
    public void setReceiverAccount(String receiverAccount) { this.receiverAccount = receiverAccount; }

    public String getBankCode() { return bankCode; }
    public void setBankCode(String bankCode) { this.bankCode = bankCode; }

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getSaleType() { return saleType; }
    public void setSaleType(String saleType) { this.saleType = saleType; }

    public boolean isSynced() { return synced; }
    public void setSynced(boolean synced) { this.synced = synced; }

    public boolean isMarkedForDelete() { return markedForDelete; }
    public void setMarkedForDelete(boolean markedForDelete) { this.markedForDelete = markedForDelete; }

    public boolean isEditing() { return editing; }
    public void setEditing(boolean editing) { this.editing = editing; }
}
