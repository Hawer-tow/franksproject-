package com.example.MobileDb.payments;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;

class PaymentFormatTest {

    @Test
    void normalisesKenyanNumbers() {
        assertEquals("254712345678", PaymentFormat.kenyanMsisdn("0712345678"));
        assertEquals("254712345678", PaymentFormat.kenyanMsisdn("712345678"));
        assertEquals("254712345678", PaymentFormat.kenyanMsisdn("+254 712 345 678"));
        assertEquals("254712345678", PaymentFormat.kenyanMsisdn("254-712-345-678"));
        assertEquals("254110345678", PaymentFormat.kenyanMsisdn("0110345678"));
    }

    @Test
    void rejectsNonKenyanOrMalformedNumbers() {
        assertThrows(IllegalArgumentException.class, () -> PaymentFormat.kenyanMsisdn(null));
        assertThrows(IllegalArgumentException.class, () -> PaymentFormat.kenyanMsisdn("0772123456x"));
        assertThrows(IllegalArgumentException.class, () -> PaymentFormat.kenyanMsisdn("256772123456")); // Ugandan
        assertThrows(IllegalArgumentException.class, () -> PaymentFormat.kenyanMsisdn("07123"));
    }

    @Test
    void amountsFollowProviderRules() {
        assertEquals(new BigDecimal("1500.50"), PaymentFormat.positiveAmount("1,500.50", "Amount"));
        assertThrows(IllegalArgumentException.class, () -> PaymentFormat.positiveAmount("0", "Amount"));
        assertThrows(IllegalArgumentException.class, () -> PaymentFormat.positiveAmount("abc", "Amount"));
        assertEquals(101, PaymentFormat.wholeShillings(new BigDecimal("100.5")));   // Daraja: whole shillings
        assertThrows(IllegalArgumentException.class, () -> PaymentFormat.wholeShillings(new BigDecimal("0.4")));
        assertEquals("500.00", PaymentFormat.twoDecimals(new BigDecimal("500")));    // Jenga: "500.00"
        assertNull(PaymentFormat.optionalRate(""));
    }

    @Test
    void textIsCutToProviderLimits() {
        assertEquals("Mpesa STK Pus", PaymentFormat.limit("Mpesa STK Push", 13, "x")); // TransactionDesc max 13
        assertEquals("Payment", PaymentFormat.limit("  ", 13, "Payment"));
    }

    @Test
    void referencesAreUpperCaseAlphanumeric() {
        String ref = PaymentFormat.reference(6);
        assertEquals(6, ref.length());
        assertTrue(ref.matches("[A-Z2-9]{6}"));
    }

    @Test
    void darajaTimestampUsesKenyanTime() {
        Clock utcNoon = Clock.fixed(Instant.parse("2026-11-17T09:00:00Z"), ZoneOffset.UTC);
        assertEquals("20261117120000", PaymentFormat.darajaTimestamp(utcNoon)); // UTC+3
    }

    @Test
    void jengaNamesAreNormalised() {
        assertEquals("Safaricom", PaymentFormat.jengaTelco("MPESA"));
        assertEquals("Equitel", PaymentFormat.jengaTelco("equitel"));
        assertThrows(IllegalArgumentException.class, () -> PaymentFormat.jengaTelco("Airtel"));
        assertEquals("Mpesa", PaymentFormat.jengaWallet("M-PESA"));
        assertEquals("Mpesa", PaymentFormat.jengaWallet(null));
    }
}
