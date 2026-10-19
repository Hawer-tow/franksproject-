package com.example.MobileDb.service.dto;

/**
 * DTO representing the STK Push request payload for Jenga HQ.
 * Contains Merchant and Payment sections.
 */
public class EquityStkPushRequest {

    private Merchant merchant;
    private Payment payment;

    public Merchant getMerchant() { return merchant; }
    public void setMerchant(Merchant merchant) { this.merchant = merchant; }

    public Payment getPayment() { return payment; }
    public void setPayment(Payment payment) { this.payment = payment; }

    @Override
    public String toString() {
        return "EquityStkPushRequest{" +
                "merchant=" + merchant +
                ", payment=" + payment +
                '}';
    }

    // Merchant section
    public static class Merchant {
        private String accountNumber;
        private String countryCode;
        private String name;

        public String getAccountNumber() { return accountNumber; }
        public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

        public String getCountryCode() { return countryCode; }
        public void setCountryCode(String countryCode) { this.countryCode = countryCode; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        @Override
        public String toString() {
            return "Merchant{" +
                    "accountNumber='" + accountNumber + '\'' +
                    ", countryCode='" + countryCode + '\'' +
                    ", name='" + name + '\'' +
                    '}';
        }
    }

    // Payment section
    public static class Payment {
        private String ref;
        private String amount;       // must be formatted with two decimals (e.g. "5.00")
        private String currency;     // e.g. "KES"
        private String telco;        // must match HQ exactly, e.g. "Safaricom"
        private String mobileNumber; // normalized to international format
        private String date;         // ISO date string
        private String callBackUrl;  // ngrok or production callback URL
        private String pushType;     // e.g. "USSD"

        public String getRef() { return ref; }
        public void setRef(String ref) { this.ref = ref; }

        public String getAmount() { return amount; }
        public void setAmount(String amount) { this.amount = amount; }

        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }

        public String getTelco() { return telco; }
        public void setTelco(String telco) { this.telco = telco; }

        public String getMobileNumber() { return mobileNumber; }
        public void setMobileNumber(String mobileNumber) { this.mobileNumber = mobileNumber; }

        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }

        public String getCallBackUrl() { return callBackUrl; }
        public void setCallBackUrl(String callBackUrl) { this.callBackUrl = callBackUrl; }

        public String getPushType() { return pushType; }
        public void setPushType(String pushType) { this.pushType = pushType; }

        @Override
        public String toString() {
            return "Payment{" +
                    "ref='" + ref + '\'' +
                    ", amount='" + amount + '\'' +
                    ", currency='" + currency + '\'' +
                    ", telco='" + telco + '\'' +
                    ", mobileNumber='" + mobileNumber + '\'' +
                    ", date='" + date + '\'' +
                    ", callBackUrl='" + callBackUrl + '\'' +
                    ", pushType='" + pushType + '\'' +
                    '}';
        }
    }
}
