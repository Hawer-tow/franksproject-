package com.example.MobileDb.service.dto;

public class EquityAccountSendMoneyRequest {

    private Source source;
    private Destination destination;
    private Transfer transfer;

    // Getters and Setters for Main Class
    public Source getSource() { return source; }
    public void setSource(Source source) { this.source = source; }

    public Destination getDestination() { return destination; }
    public void setDestination(Destination destination) { this.destination = destination; }

    public Transfer getTransfer() { return transfer; }
    public void setTransfer(Transfer transfer) { this.transfer = transfer; }

    public static class Source {
        private String countryCode;
        private String name;
        private String accountNumber;

        // Getters and Setters
        public String getCountryCode() { return countryCode; }
        public void setCountryCode(String countryCode) { this.countryCode = countryCode; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getAccountNumber() { return accountNumber; }
        public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
    }

    public static class Destination {
        private String type;
        private String countryCode;
        private String name;
        private String accountNumber;

        // Getters and Setters
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public String getCountryCode() { return countryCode; }
        public void setCountryCode(String countryCode) { this.countryCode = countryCode; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getAccountNumber() { return accountNumber; }
        public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
    }

    public static class Transfer {
        private String type;
        private String amount;
        private String currencyCode;
        private String reference;
        private String date;
        private String description;

        // Getters and Setters
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public String getAmount() { return amount; }
        public void setAmount(String amount) { this.amount = amount; }

        public String getCurrencyCode() { return currencyCode; }
        public void setCurrencyCode(String currencyCode) { this.currencyCode = currencyCode; }

        public String getReference() { return reference; }
        public void setReference(String reference) { this.reference = reference; }

        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }
}
