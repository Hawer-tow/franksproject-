package com.example.MobileDb.payments;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/** Payloads follow the examples on developer.jengahq.io (API v3). */
class JengaCallbacksTest {

    private static Map<String, Object> json(String s) throws Exception {
        return new ObjectMapper().readValue(s, new TypeReference<Map<String, Object>>() {});
    }

    @Test
    void completedPromptIsSuccess() throws Exception {
        JengaCallbacks.JengaResult r = JengaCallbacks.parseStkPush(json("""
                {"status":true,"code":3,"message":"Successful","transactionReference":"AB12CD",
                 "telcoReference":"RKT1ABC2DE","mobileNumber":"254722000000","currency":"KES",
                 "requestAmount":"500.00","debitedAmount":"500.00","charge":"0","telco":"Safaricom"}
                """));
        assertEquals("AB12CD", r.reference());
        assertEquals(PaymentStatus.SUCCESS, r.status());
        assertEquals("RKT1ABC2DE", r.providerReference());
    }

    @Test
    void pendingCodeZeroIsNotSuccess() {
        // the old handler treated "0" as paid; in Jenga's prompt callbacks 0 means pending
        assertEquals(PaymentStatus.PENDING, JengaCallbacks.stkStatus("0"));
        assertEquals(PaymentStatus.PENDING, JengaCallbacks.stkStatus("-1"));
        assertEquals(PaymentStatus.SUCCESS, JengaCallbacks.stkStatus("4"));
        assertEquals(PaymentStatus.CANCELLED, JengaCallbacks.stkStatus("5"));
        assertEquals(PaymentStatus.FAILED, JengaCallbacks.stkStatus("7"));
    }

    @Test
    void failedPromptWithStringCode() throws Exception {
        JengaCallbacks.JengaResult r = JengaCallbacks.parseStkPush(json("""
                {"status":false,"code":"1","message":"InsufficientFund","transactionReference":"ZZ99YY"}
                """));
        assertEquals(PaymentStatus.FAILED, r.status());
        assertEquals("InsufficientFund", r.message());
    }

    @Test
    void ipnShapeIsUnderstood() throws Exception {
        JengaCallbacks.JengaResult r = JengaCallbacks.parseStkPush(json("""
                {"callbackType":"IPN","customer":{"name":"A"},"transaction":{"reference":"AB12CD","status":"SUCCESS","amount":"500"}}
                """));
        assertEquals("AB12CD", r.reference());
        assertEquals(PaymentStatus.SUCCESS, r.status());
    }

    @Test
    void sendToMobileCallback() throws Exception {
        JengaCallbacks.JengaResult ok = JengaCallbacks.parseSendMobile(json("""
                {"status":true,"code":"0","message":"success","transactionReference":"REF123456789",
                 "data":{"ResponseCode":"0","ReceiverMsisdn":"254722000000","Reference":"REF123456789",
                 "ResponseDescription":"Success","ThirdPartyTranID":"RKT9","ReceiverName":"JOHN"}}
                """));
        assertEquals(PaymentStatus.SUCCESS, ok.status());
        assertEquals("REF123456789", ok.reference());

        JengaCallbacks.JengaResult bad = JengaCallbacks.parseSendMobile(json("""
                {"status":false,"transactionReference":"REF1","data":{"ResponseCode":"2001","ResponseDescription":"Invalid"}}
                """));
        assertEquals(PaymentStatus.FAILED, bad.status());
    }

    @Test
    void internalTransferResponseIsFinal() {
        assertEquals(PaymentStatus.SUCCESS, JengaCallbacks.parseInternalTransfer(
                "{\"status\":true,\"code\":0,\"message\":\"success\",\"reference\":\"R1\",\"data\":{\"transactionId\":\"T1\",\"status\":\"SUCCESS\"}}").status());
        assertEquals(PaymentStatus.PENDING, JengaCallbacks.parseInternalTransfer(
                "{\"status\":true,\"code\":0,\"data\":{\"status\":\"PENDING\"}}").status());
        assertEquals(PaymentStatus.FAILED, JengaCallbacks.parseInternalTransfer(
                "{\"status\":false,\"code\":110,\"message\":\"Insufficient funds\"}").status());
    }

    @Test
    void acknowledgementDetection() {
        assertTrue(JengaCallbacks.isAccepted("{\"status\":true,\"code\":-1,\"message\":\"Processing\"}"));
        assertFalse(JengaCallbacks.isAccepted("{\"status\":false,\"code\":400,\"message\":\"Bad\"}"));
        assertEquals("Bad", JengaCallbacks.message("{\"status\":false,\"message\":\"Bad\"}"));
    }
}
