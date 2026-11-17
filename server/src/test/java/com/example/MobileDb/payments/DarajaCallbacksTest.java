package com.example.MobileDb.payments;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/** Payloads follow the samples in Safaricom's Daraja documentation. */
class DarajaCallbacksTest {

    private static Map<String, Object> json(String s) throws Exception {
        return new ObjectMapper().readValue(s, new TypeReference<Map<String, Object>>() {});
    }

    @Test
    void readsSuccessfulStkCallback() throws Exception {
        DarajaCallbacks.StkResult r = DarajaCallbacks.parseStk(json("""
                {"Body":{"stkCallback":{"MerchantRequestID":"29115-34620561-1","CheckoutRequestID":"ws_CO_191220191020363925",
                 "ResultCode":0,"ResultDesc":"The service request is processed successfully.",
                 "CallbackMetadata":{"Item":[{"Name":"Amount","Value":1.00},{"Name":"MpesaReceiptNumber","Value":"NLJ7RT61SV"},
                 {"Name":"TransactionDate","Value":20191219102115},{"Name":"PhoneNumber","Value":254708374149}]}}}}
                """));
        assertEquals("ws_CO_191220191020363925", r.checkoutRequestId());
        assertEquals(PaymentStatus.SUCCESS, r.status());
        assertEquals("NLJ7RT61SV", r.receipt());
        assertEquals("254708374149", r.phone());
        assertEquals(0, new BigDecimal("1.00").compareTo(r.amount()));
    }

    @Test
    void mapsCancelledAndUnansweredPrompts() throws Exception {
        DarajaCallbacks.StkResult cancelled = DarajaCallbacks.parseStk(json("""
                {"Body":{"stkCallback":{"MerchantRequestID":"1","CheckoutRequestID":"ws_CO_1","ResultCode":1032,
                 "ResultDesc":"Request cancelled by user"}}}
                """));
        assertEquals(PaymentStatus.CANCELLED, cancelled.status());
        assertNull(cancelled.receipt());
        assertEquals(PaymentStatus.TIMEOUT, DarajaCallbacks.stkStatus(1037));
        assertEquals(PaymentStatus.FAILED, DarajaCallbacks.stkStatus(2001)); // wrong PIN
    }

    @Test
    void readsB2cResult() throws Exception {
        DarajaCallbacks.B2cResult r = DarajaCallbacks.parseB2c(json("""
                {"Result":{"ResultType":0,"ResultCode":0,"ResultDesc":"The service request is processed successfully.",
                 "OriginatorConversationID":"FMABC123","ConversationID":"AG_20191219_00005797af5d7d75f652",
                 "TransactionID":"NLJ41HAY6Q","ResultParameters":{"ResultParameter":[
                 {"Key":"TransactionAmount","Value":10},{"Key":"TransactionReceipt","Value":"NLJ41HAY6Q"},
                 {"Key":"ReceiverPartyPublicName","Value":"254708374149 - John Doe"}]}}}
                """));
        assertEquals("FMABC123", r.originatorConversationId());
        assertEquals(PaymentStatus.SUCCESS, r.status());
        assertEquals("NLJ41HAY6Q", r.receipt());
        assertEquals("254708374149 - John Doe", r.receiverName());
    }

    @Test
    void readsFailedB2cWithSingleParameter() throws Exception {
        DarajaCallbacks.B2cResult r = DarajaCallbacks.parseB2c(json("""
                {"Result":{"ResultType":0,"ResultCode":2001,"ResultDesc":"The initiator information is invalid.",
                 "OriginatorConversationID":"FMXYZ","ConversationID":"AG_1","TransactionID":"NLJ0000000",
                 "ResultParameters":{"ResultParameter":{"Key":"TransactionAmount","Value":5}}}}
                """));
        assertEquals(PaymentStatus.FAILED, r.status());
        assertEquals(0, new BigDecimal("5").compareTo(r.amount()));
    }

    @Test
    void rejectsUnrelatedPayloads() {
        assertThrows(IllegalArgumentException.class, () -> DarajaCallbacks.parseStk(Map.of("foo", "bar")));
        assertThrows(IllegalArgumentException.class, () -> DarajaCallbacks.parseB2c(Map.of("foo", "bar")));
    }
}
