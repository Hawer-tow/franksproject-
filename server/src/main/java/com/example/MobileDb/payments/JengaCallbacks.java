package com.example.MobileDb.payments;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.Map;

import static com.example.MobileDb.payments.DarajaCallbacks.decimal;
import static com.example.MobileDb.payments.DarajaCallbacks.map;
import static com.example.MobileDb.payments.DarajaCallbacks.text;

/**
 * Reads Equity Jenga responses and callbacks (developer.jengahq.io, API v3).
 *
 * STK/USSD push callback (to payment.callBackUrl):
 *   {"status":true,"code":3,"message":"Successful","transactionReference":"ABC123","telcoReference":"QK..",
 *    "mobileNumber":"2547..","currency":"KES","requestAmount":"500.00","debitedAmount":"500.00","charge":"0","telco":"Safaricom"}
 *   code 3 = completed/credited, 4 = paid but awaiting settlement to the merchant,
 *   0 = pending, 2 = awaiting third-party settlement, 1 / 7 = failed / rejected, 5 / 6 = cancelled.
 *   Portal IPNs arrive as {"callbackType":"IPN","transaction":{"reference":..,"status":"SUCCESS",..}}.
 *
 * Send to mobile wallet callback (to transfer.callbackUrl):
 *   {"status":true,"code":"0","message":"success","transactionReference":"REF..",
 *    "data":{"ResponseCode":"0","Reference":..,"ThirdPartyTranID":..,"ReceiverName":..}}
 *   data.ResponseCode "0" = delivered.
 *
 * Within-Equity transfer has no callback; the response is final:
 *   {"status":true,"code":0,"message":..,"reference":..,"data":{"transactionId":..,"status":"SUCCESS|PENDING|FAILED"}}
 *
 * Request acknowledgements (STK push, send to mobile) return status true with code -1: accepted, wait for the callback.
 */
public final class JengaCallbacks {

    private static final ObjectMapper JSON = new ObjectMapper();

    private JengaCallbacks() {}

    public record JengaResult(String reference, String status, String code, String message,
                              String providerReference, BigDecimal amount) {}

    /** Payment-prompt callback, flat or IPN shape. */
    public static JengaResult parseStkPush(Map<String, Object> payload) {
        Map<String, Object> txn = map(payload, "transaction");
        if (!txn.isEmpty()) {
            // Portal IPN
            String s = text(txn.get("status"));
            String status = "SUCCESS".equalsIgnoreCase(s) ? PaymentStatus.SUCCESS : PaymentStatus.FAILED;
            return new JengaResult(
                    firstText(txn, "reference", "billNumber", "orderReference"),
                    status, s, text(txn.get("remarks")),
                    firstText(txn, "paymentReference", "serviceReference"),
                    decimal(txn.get("amount")));
        }
        String code = text(payload.get("code"));
        return new JengaResult(
                firstText(payload, "transactionReference", "reference"),
                stkStatus(code),
                code,
                text(payload.get("message")),
                text(payload.get("telcoReference")),
                decimal(firstText(payload, "debitedAmount", "requestAmount")));
    }

    /** Send-to-mobile-wallet callback. */
    public static JengaResult parseSendMobile(Map<String, Object> payload) {
        Map<String, Object> data = map(payload, "data");
        String responseCode = text(data.get("ResponseCode"));
        String status;
        if ("0".equals(responseCode)) {
            status = PaymentStatus.SUCCESS;
        } else if (responseCode != null || Boolean.FALSE.equals(payload.get("status"))
                || "false".equalsIgnoreCase(text(payload.get("status")))) {
            status = PaymentStatus.FAILED;
        } else {
            status = PaymentStatus.PENDING;
        }
        String message = text(data.get("ResponseDescription"));
        return new JengaResult(
                firstText(payload, "transactionReference", "reference") != null
                        ? firstText(payload, "transactionReference", "reference")
                        : text(data.get("Reference")),
                status,
                responseCode != null ? responseCode : text(payload.get("code")),
                message != null ? message : text(payload.get("message")),
                text(data.get("ThirdPartyTranID")),
                null);
    }

    /** Within-Equity transfer response (synchronous, final). */
    public static JengaResult parseInternalTransfer(String responseBody) {
        Map<String, Object> payload = readJson(responseBody);
        Map<String, Object> data = map(payload, "data");
        String dataStatus = text(data.get("status"));
        String status;
        if ("SUCCESS".equalsIgnoreCase(dataStatus)) {
            status = PaymentStatus.SUCCESS;
        } else if ("PENDING".equalsIgnoreCase(dataStatus)) {
            status = PaymentStatus.PENDING;
        } else if (dataStatus == null && isTrue(payload.get("status")) && "0".equals(text(payload.get("code")))) {
            status = PaymentStatus.SUCCESS;
        } else {
            status = PaymentStatus.FAILED;
        }
        return new JengaResult(text(payload.get("reference")), status, text(payload.get("code")),
                text(payload.get("message")), text(data.get("transactionId")), null);
    }

    /** True when a request (STK push / send to mobile) was accepted for processing. */
    public static boolean isAccepted(String responseBody) {
        Map<String, Object> payload = readJson(responseBody);
        return isTrue(payload.get("status"));
    }

    /** Human-readable message from a Jenga response or error body. */
    public static String message(String responseBody) {
        Map<String, Object> payload = readJson(responseBody);
        String m = text(payload.get("message"));
        return m != null ? m : responseBody;
    }

    /** Maps an STK/USSD push callback code to our status. */
    public static String stkStatus(String code) {
        if (code == null) return PaymentStatus.PENDING;
        switch (code.trim()) {
            case "3":
            case "4": // customer paid; settlement to the merchant account still in progress
                return PaymentStatus.SUCCESS;
            case "0":
            case "2":
            case "-1":
                return PaymentStatus.PENDING;
            case "5":
            case "6":
                return PaymentStatus.CANCELLED;
            default:
                return PaymentStatus.FAILED;
        }
    }

    public static Map<String, Object> readJson(String body) {
        if (body == null || body.isBlank()) return Map.of();
        try {
            return JSON.readValue(body, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            return Map.of();
        }
    }

    private static boolean isTrue(Object value) {
        return Boolean.TRUE.equals(value) || "true".equalsIgnoreCase(text(value));
    }

    private static String firstText(Map<String, Object> m, String... keys) {
        for (String k : keys) {
            String v = text(m.get(k));
            if (v != null) return v;
        }
        return null;
    }
}
