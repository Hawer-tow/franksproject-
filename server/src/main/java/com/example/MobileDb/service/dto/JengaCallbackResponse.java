package com.example.MobileDb.service.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class JengaCallbackResponse {

    // --- FLAT FIELDS (Direct STK Callback) ---
    @JsonAlias({"transactionReference", "reference"})
    private String transactionReference;
    
    private String message;
    private String status; // Can be boolean or String, we'll handle as String

    // --- NESTED FIELDS (IPN Callback) ---
    private Transaction transaction;
    private Customer customer;
    private String callbackType;

    // --- GETTERS AND SETTERS ---

    public String getEffectiveReference() {
        // If nested transaction exists, use its reference; otherwise use flat field
        if (transaction != null && transaction.getReference() != null) {
            return transaction.getReference();
        }
        return transactionReference;
    }

    public String getEffectiveStatus() {
        if (transaction != null && transaction.getStatus() != null) {
            return transaction.getStatus();
        }
        return status;
    }

    // Standard Getters/Setters
    public String getTransactionReference() { return transactionReference; }
    public void setTransactionReference(String transactionReference) { this.transactionReference = transactionReference; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Transaction getTransaction() { return transaction; }
    public void setTransaction(Transaction transaction) { this.transaction = transaction; }

    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }

    // --- INNER CLASSES ---

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Transaction {
        private String reference;
        private String status;
        private String remarks;

        public String getReference() { return reference; }
        public void setReference(String reference) { this.reference = reference; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getRemarks() { return remarks; }
        public void setRemarks(String remarks) { this.remarks = remarks; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Customer {
        private String mobileNumber;
        public String getMobileNumber() { return mobileNumber; }
        public void setMobileNumber(String mobileNumber) { this.mobileNumber = mobileNumber; }
    }
}
