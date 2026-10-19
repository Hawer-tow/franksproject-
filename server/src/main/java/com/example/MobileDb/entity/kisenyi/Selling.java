package com.example.MobileDb.entity.kisenyi;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "selling")
public class Selling {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Selling_ID")
    private Integer sellingId;

    @Column(name = "Selling_date", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date sellingDate;

    @Column(name = "Selling_amount")
    private Double sellingAmount;

    @Column(name = "Selling_rate")
    private Double sellingRate;

    @Column(name = "Receiving_Country", length = 50)
    private String receivingCountry;

    @Column(name = "Phone_No", length = 15)
    private String phoneNo;

    @Column(name = "Recipient_name", length = 70)
    private String recipientName;

    @Column(name = "Sell_Description", length = 255)
    private String sellDescription;

    // --- Bank-related fields ---
    @Column(name = "Sender_Account", length = 50)
    private String senderAccount;

    @Column(name = "Receiver_Account", length = 50)
    private String receiverAccount;

    @Column(name = "Bank_Code", length = 20)
    private String bankCode;

    @Column(name = "Transaction_ID", length = 100)
    private String transactionId;

    @Column(name = "Currency", length = 10)
    private String currency;

    @Column(name = "Status", length = 50)
    private String status;

    // --- Provider debug fields ---
    @Column(name = "provider_response", columnDefinition = "TEXT")
    private String providerResponse;

    @Column(name = "provider_http_status", length = 255)
    private String providerHttpStatus;

    // --- Existing DB columns for Daraja correlation ---
    @Column(name = "originator_conversation_id", length = 255)
    private String originatorConversationId; // provider originator ID

    @Column(name = "ConversationID", length = 100)
    private String conversationId; // provider ConversationID

    // --- New column for provenance ---
    @Column(name = "SaleType", length = 30)
    private String saleType; // USER_INITIATED_BANK, USER_INITIATED_MPESA, BANK, MPESA

    // --- Transient field for app-generated ID (not persisted) ---
    @Transient
    private String clientOriginatorId; // APP_<uuid>

    public Selling() {}

    // --- Getters and setters ---
    public Integer getSellingId() { return sellingId; }
    public void setSellingId(Integer sellingId) { this.sellingId = sellingId; }

    public Date getSellingDate() { return sellingDate; }
    public void setSellingDate(Date sellingDate) { this.sellingDate = sellingDate; }

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

    public String getProviderResponse() { return providerResponse; }
    public void setProviderResponse(String providerResponse) { this.providerResponse = providerResponse; }

    public String getProviderHttpStatus() { return providerHttpStatus; }
    public void setProviderHttpStatus(String providerHttpStatus) { this.providerHttpStatus = providerHttpStatus; }

    public String getOriginatorConversationId() { return originatorConversationId; }
    public void setOriginatorConversationId(String originatorConversationId) { this.originatorConversationId = originatorConversationId; }

    public String getConversationId() { return conversationId; }
    public void setConversationId(String conversationId) { this.conversationId = conversationId; }

    public String getSaleType() { return saleType; }
    public void setSaleType(String saleType) { this.saleType = saleType; }

    public String getClientOriginatorId() { return clientOriginatorId; }
    public void setClientOriginatorId(String clientOriginatorId) { this.clientOriginatorId = clientOriginatorId; }
}



/*


SellingController
DarajaPaymentService
DarajaCallbackController
PaymentResponse
PaymentRequest
SecurityUtils
DarajaProperties

*/
