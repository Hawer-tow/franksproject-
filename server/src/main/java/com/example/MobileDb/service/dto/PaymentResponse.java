package com.example.MobileDb.service.dto;

import java.util.Map;

public class PaymentResponse {
    private Integer status;
    private String message;
    private Map<String, Object> raw;

    // Safaricom-specific fields for easier debugging and correlation
    private String resultCode;
    private String resultDesc;
    private String transactionId;
    private String originatorConversationID;
    private String conversationID;
    private String responseCode;
    private String responseDescription;

    // --- New field for provenance ---
    private String saleType; // MPESA, BANK, USER_INITIATED_BANK, USER_INITIATED_MPESA

    public PaymentResponse() {}

    public PaymentResponse(Integer status, String message, Map<String, Object> raw) {
        this.status = status;
        this.message = message;
        this.raw = raw;

        if (raw != null) {
            // Top-level fields often returned by Daraja
            Object conv = raw.get("ConversationID");
            Object originator = raw.get("OriginatorConversationID");
            Object respCode = raw.get("ResponseCode");
            Object respDesc = raw.get("ResponseDescription");

            this.conversationID = conv != null ? conv.toString() : null;
            this.originatorConversationID = originator != null ? originator.toString() : null;
            this.responseCode = respCode != null ? respCode.toString() : null;
            this.responseDescription = respDesc != null ? respDesc.toString() : null;

            // Some responses include a nested Result block
            Object resultObj = raw.get("Result");
            if (resultObj instanceof Map) {
                Map<?, ?> resultMap = (Map<?, ?>) resultObj;
                Object rc = resultMap.get("ResultCode");
                Object rd = resultMap.get("ResultDesc");
                Object tx = resultMap.get("TransactionID");

                this.resultCode = rc != null ? rc.toString() : null;
                this.resultDesc = rd != null ? rd.toString() : null;
                this.transactionId = tx != null ? tx.toString() : null;

                // If originator/conversation not at top-level, try nested Result
                if (this.originatorConversationID == null) {
                    Object nestedOriginator = resultMap.get("OriginatorConversationID");
                    this.originatorConversationID = nestedOriginator != null ? nestedOriginator.toString() : this.originatorConversationID;
                }
                if (this.conversationID == null) {
                    Object nestedConversation = resultMap.get("ConversationID");
                    this.conversationID = nestedConversation != null ? nestedConversation.toString() : this.conversationID;
                }
            }

            // Some endpoints return ResponseCode/ResponseDescription at top-level and TransactionID inside other nested structures
            if (this.transactionId == null) {
                Object txTop = raw.get("TransactionID");
                this.transactionId = txTop != null ? txTop.toString() : this.transactionId;
            }
        }
    }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Map<String, Object> getRaw() { return raw; }
    public void setRaw(Map<String, Object> raw) { this.raw = raw; }

    public String getResultCode() { return resultCode; }
    public void setResultCode(String resultCode) { this.resultCode = resultCode; }

    public String getResultDesc() { return resultDesc; }
    public void setResultDesc(String resultDesc) { this.resultDesc = resultDesc; }

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public String getOriginatorConversationID() { return originatorConversationID; }
    public void setOriginatorConversationID(String originatorConversationID) { this.originatorConversationID = originatorConversationID; }

    public String getConversationID() { return conversationID; }
    public void setConversationID(String conversationID) { this.conversationID = conversationID; }

    public String getResponseCode() { return responseCode; }
    public void setResponseCode(String responseCode) { this.responseCode = responseCode; }

    public String getResponseDescription() { return responseDescription; }
    public void setResponseDescription(String responseDescription) { this.responseDescription = responseDescription; }

    public String getSaleType() { return saleType; }
    public void setSaleType(String saleType) { this.saleType = saleType; }

    @Override
    public String toString() {
        return "PaymentResponse{" +
                "status=" + status +
                ", message='" + message + '\'' +
                ", resultCode='" + resultCode + '\'' +
                ", resultDesc='" + resultDesc + '\'' +
                ", transactionId='" + transactionId + '\'' +
                ", originatorConversationID='" + originatorConversationID + '\'' +
                ", conversationID='" + conversationID + '\'' +
                ", responseCode='" + responseCode + '\'' +
                ", responseDescription='" + responseDescription + '\'' +
                ", saleType='" + saleType + '\'' +
                '}';
    }
}
