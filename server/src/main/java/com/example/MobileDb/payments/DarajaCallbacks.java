package com.example.MobileDb.payments;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Reads Safaricom Daraja callbacks.
 *
 * STK push (Lipa na M-Pesa Online) result:
 *   {"Body":{"stkCallback":{"MerchantRequestID":..,"CheckoutRequestID":..,"ResultCode":0,"ResultDesc":..,
 *     "CallbackMetadata":{"Item":[{"Name":"Amount","Value":1},{"Name":"MpesaReceiptNumber","Value":"QK.."},
 *     {"Name":"TransactionDate","Value":20260101120000},{"Name":"PhoneNumber","Value":2547..}]}}}}
 *   ResultCode 0 = paid, 1032 = cancelled by customer, 1037 / 1019 = no response / expired, others = failed.
 *
 * B2C result:
 *   {"Result":{"ResultType":0,"ResultCode":0,"ResultDesc":..,"OriginatorConversationID":..,"ConversationID":..,
 *     "TransactionID":..,"ResultParameters":{"ResultParameter":[{"Key":"TransactionAmount","Value":10},
 *     {"Key":"TransactionReceipt","Value":"QK.."},{"Key":"ReceiverPartyPublicName","Value":"2547.. - Name"}, ...]}}}
 */
public final class DarajaCallbacks {

    private DarajaCallbacks() {}

    public record StkResult(String merchantRequestId, String checkoutRequestId, int resultCode,
                            String resultDesc, BigDecimal amount, String receipt, String phone) {
        public String status() {
            return stkStatus(resultCode);
        }
    }

    public record B2cResult(String originatorConversationId, String conversationId, String transactionId,
                            int resultCode, String resultDesc, BigDecimal amount, String receipt,
                            String receiverName) {
        public String status() {
            return resultCode == 0 ? PaymentStatus.SUCCESS : PaymentStatus.FAILED;
        }
    }

    /** Maps an STK push ResultCode to our status. */
    public static String stkStatus(int resultCode) {
        switch (resultCode) {
            case 0:
                return PaymentStatus.SUCCESS;
            case 1032:
                return PaymentStatus.CANCELLED;
            case 1037:
            case 1019:
                return PaymentStatus.TIMEOUT;
            default:
                return PaymentStatus.FAILED;
        }
    }

    public static StkResult parseStk(Map<String, Object> payload) {
        Map<String, Object> body = map(payload, "Body");
        Map<String, Object> cb = map(body, "stkCallback");
        if (cb.isEmpty()) {
            throw new IllegalArgumentException("Not an STK push callback: Body.stkCallback missing");
        }
        Map<String, Object> items = new HashMap<>();
        Object list = map(cb, "CallbackMetadata").get("Item");
        if (list instanceof Collection<?> c) {
            for (Object o : c) {
                if (o instanceof Map<?, ?> item && item.get("Name") != null) {
                    items.put(item.get("Name").toString(), item.get("Value"));
                }
            }
        }
        return new StkResult(
                text(cb.get("MerchantRequestID")),
                text(cb.get("CheckoutRequestID")),
                integer(cb.get("ResultCode")),
                text(cb.get("ResultDesc")),
                decimal(items.get("Amount")),
                text(items.get("MpesaReceiptNumber")),
                text(items.get("PhoneNumber")));
    }

    public static B2cResult parseB2c(Map<String, Object> payload) {
        Map<String, Object> result = map(payload, "Result");
        if (result.isEmpty()) {
            throw new IllegalArgumentException("Not a B2C result: Result missing");
        }
        Map<String, Object> params = new HashMap<>();
        Object list = map(result, "ResultParameters").get("ResultParameter");
        if (list instanceof Map<?, ?> single) {
            list = java.util.List.of(single);
        }
        if (list instanceof Collection<?> c) {
            for (Object o : c) {
                if (o instanceof Map<?, ?> p && p.get("Key") != null) {
                    params.put(p.get("Key").toString(), p.get("Value"));
                }
            }
        }
        String receipt = text(params.get("TransactionReceipt"));
        String transactionId = text(result.get("TransactionID"));
        return new B2cResult(
                text(result.get("OriginatorConversationID")),
                text(result.get("ConversationID")),
                transactionId,
                integer(result.get("ResultCode")),
                text(result.get("ResultDesc")),
                decimal(params.get("TransactionAmount")),
                receipt != null ? receipt : transactionId,
                text(params.get("ReceiverPartyPublicName")));
    }

    // ---------------------------------------------------------------- helpers

    @SuppressWarnings("unchecked")
    static Map<String, Object> map(Map<String, Object> parent, String key) {
        if (parent == null) return Map.of();
        Object value = parent.get(key);
        return value instanceof Map ? (Map<String, Object>) value : Map.of();
    }

    static String text(Object value) {
        if (value == null) return null;
        if (value instanceof Number n && !(value instanceof BigDecimal)) {
            // phone numbers and dates arrive as JSON numbers; avoid "2.547E11"
            return new BigDecimal(n.toString()).toPlainString();
        }
        String s = value.toString().trim();
        return s.isEmpty() ? null : s;
    }

    static int integer(Object value) {
        if (value == null) return -1;
        try {
            return new BigDecimal(value.toString().trim()).intValue();
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    static BigDecimal decimal(Object value) {
        if (value == null) return null;
        try {
            return new BigDecimal(value.toString().trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
