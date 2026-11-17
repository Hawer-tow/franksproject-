package com.example.MobileDb.entity.kisenyi;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * One row per request sent to Safaricom (Daraja) or Equity (Jenga): what was asked, what the
 * provider answered, and the final result from the callback. Buys and sales are only recorded
 * (or marked successful) from here once the provider confirms the money moved.
 */
@Entity
@Table(name = "provider_transactions", indexes = {
        @Index(name = "idx_provider_txn_reference", columnList = "reference", unique = true),
        @Index(name = "idx_provider_txn_provider_request", columnList = "provider_request_id")
})
public class ProviderTransaction {

    public static final String MPESA = "MPESA";
    public static final String EQUITY = "EQUITY";

    /** M-Pesa payment prompt to a customer's phone (money in). */
    public static final String MPESA_STK_PUSH = "MPESA_STK_PUSH";
    /** M-Pesa business-to-customer payment (money out). */
    public static final String MPESA_B2C = "MPESA_B2C";
    /** Equity payment prompt to a Safaricom or Equitel phone (money in). */
    public static final String EQUITY_STK_PUSH = "EQUITY_STK_PUSH";
    /** Equity account to a mobile wallet (money out). */
    public static final String EQUITY_SEND_MOBILE = "EQUITY_SEND_MOBILE";
    /** Equity account to another Equity account (money out). */
    public static final String EQUITY_INTERNAL_TRANSFER = "EQUITY_INTERNAL_TRANSFER";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 10)
    private String provider;

    @Column(nullable = false, length = 30)
    private String kind;

    /** Our reference: Jenga payment/transfer reference, or the B2C OriginatorConversationID. */
    @Column(nullable = false, length = 64)
    private String reference;

    /** Provider id used to match callbacks: CheckoutRequestID (STK) or ConversationID (B2C). */
    @Column(name = "provider_request_id", length = 100)
    private String providerRequestId;

    /** Secondary provider id: MerchantRequestID (STK) or Jenga transactionId. */
    @Column(name = "provider_secondary_id", length = 100)
    private String providerSecondaryId;

    @Column(length = 20)
    private String phone;

    @Column(length = 50)
    private String account;

    @Column(precision = 14, scale = 2)
    private BigDecimal amount;

    @Column(precision = 12, scale = 4)
    private BigDecimal rate;

    @Column(length = 10)
    private String currency;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "result_code", length = 20)
    private String resultCode;

    @Column(name = "result_desc", length = 255)
    private String resultDesc;

    /** M-Pesa receipt / Equity telco or transaction reference once paid. */
    @Column(length = 100)
    private String receipt;

    @Column(name = "buying_id")
    private Integer buyingId;

    @Column(name = "selling_id")
    private Integer sellingId;

    @Column(name = "response_payload", columnDefinition = "TEXT")
    private String responsePayload;

    @Column(name = "callback_payload", columnDefinition = "TEXT")
    private String callbackPayload;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        createdAt = updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }
    public String getKind() { return kind; }
    public void setKind(String kind) { this.kind = kind; }
    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }
    public String getProviderRequestId() { return providerRequestId; }
    public void setProviderRequestId(String providerRequestId) { this.providerRequestId = providerRequestId; }
    public String getProviderSecondaryId() { return providerSecondaryId; }
    public void setProviderSecondaryId(String providerSecondaryId) { this.providerSecondaryId = providerSecondaryId; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getAccount() { return account; }
    public void setAccount(String account) { this.account = account; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public BigDecimal getRate() { return rate; }
    public void setRate(BigDecimal rate) { this.rate = rate; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getResultCode() { return resultCode; }
    public void setResultCode(String resultCode) { this.resultCode = resultCode; }
    public String getResultDesc() { return resultDesc; }
    public void setResultDesc(String resultDesc) {
        this.resultDesc = resultDesc != null && resultDesc.length() > 255 ? resultDesc.substring(0, 255) : resultDesc;
    }
    public String getReceipt() { return receipt; }
    public void setReceipt(String receipt) { this.receipt = receipt; }
    public Integer getBuyingId() { return buyingId; }
    public void setBuyingId(Integer buyingId) { this.buyingId = buyingId; }
    public Integer getSellingId() { return sellingId; }
    public void setSellingId(Integer sellingId) { this.sellingId = sellingId; }
    public String getResponsePayload() { return responsePayload; }
    public void setResponsePayload(String responsePayload) { this.responsePayload = responsePayload; }
    public String getCallbackPayload() { return callbackPayload; }
    public void setCallbackPayload(String callbackPayload) { this.callbackPayload = callbackPayload; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
