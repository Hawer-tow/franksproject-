package com.example.mobiledbclient.ui.MajorTransactions.EquityBuys;

public class StkPushResponse {
    private String checkoutRequestId;
    private String responseCode;
    private String responseDescription;

    // getters and setters
    public String getCheckoutRequestId() { return checkoutRequestId; }
    public void setCheckoutRequestId(String checkoutRequestId) { this.checkoutRequestId = checkoutRequestId; }

    public String getResponseCode() { return responseCode; }
    public void setResponseCode(String responseCode) { this.responseCode = responseCode; }

    public String getResponseDescription() { return responseDescription; }
    public void setResponseDescription(String responseDescription) { this.responseDescription = responseDescription; }
}
