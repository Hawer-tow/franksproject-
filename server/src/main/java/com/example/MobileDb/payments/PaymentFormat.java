package com.example.MobileDb.payments;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Input rules shared by the M-Pesa (Daraja) and Equity (Jenga) integrations:
 * phone numbers, amounts, references and length limits.
 */
public final class PaymentFormat {

    public static final ZoneId NAIROBI = ZoneId.of("Africa/Nairobi");

    private static final String REFERENCE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final DateTimeFormatter DARAJA_TIMESTAMP = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private PaymentFormat() {}

    /**
     * Kenyan mobile number in the 2547XXXXXXXX / 2541XXXXXXXX form both Daraja and Jenga expect.
     * Accepts 0712345678, 712345678, +254712345678, 254712345678 (spaces and dashes ignored).
     */
    public static String kenyanMsisdn(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("Phone number is required");
        }
        String digits = raw.replaceAll("[\\s\\-()]", "");
        if (digits.startsWith("+")) digits = digits.substring(1);
        if (digits.length() == 10 && digits.startsWith("0")) {
            digits = "254" + digits.substring(1);
        } else if (digits.length() == 9 && (digits.startsWith("7") || digits.startsWith("1"))) {
            digits = "254" + digits;
        }
        if (!digits.matches("254[17]\\d{8}")) {
            throw new IllegalArgumentException(
                    "Phone number must be a Kenyan mobile number, e.g. 0712345678 or 254712345678");
        }
        return digits;
    }

    /** Positive amount from user input (text or number). */
    public static BigDecimal positiveAmount(Object raw, String field) {
        if (raw == null || raw.toString().isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
        BigDecimal value;
        try {
            value = new BigDecimal(raw.toString().trim().replace(",", ""));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(field + " must be a number");
        }
        if (value.signum() <= 0) {
            throw new IllegalArgumentException(field + " must be greater than zero");
        }
        return value;
    }

    /** Optional exchange rate; null when not supplied. */
    public static BigDecimal optionalRate(Object raw) {
        if (raw == null || raw.toString().isBlank()) return null;
        return positiveAmount(raw, "Rate");
    }

    /** Daraja only accepts whole shillings (minimum 1). Rounds half up. */
    public static int wholeShillings(BigDecimal amount) {
        int whole = amount.setScale(0, RoundingMode.HALF_UP).intValueExact();
        if (whole < 1) {
            throw new IllegalArgumentException("Amount must be at least 1 shilling");
        }
        return whole;
    }

    /** Jenga amounts are strings with two decimals, e.g. "500.00". */
    public static String twoDecimals(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    /** Trimmed text cut to the provider's maximum length; fallback when empty. */
    public static String limit(String text, int max, String fallback) {
        String value = (text == null || text.isBlank()) ? fallback : text.trim();
        return value.length() <= max ? value : value.substring(0, max);
    }

    /** Random upper-case alphanumeric reference (no 0/O/1/I to avoid misreading). */
    public static String reference(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(REFERENCE_CHARS.charAt(RANDOM.nextInt(REFERENCE_CHARS.length())));
        }
        return sb.toString();
    }

    /** yyyyMMddHHmmss in Kenyan time, as used in the Daraja password. */
    public static String darajaTimestamp(Clock clock) {
        return LocalDateTime.now(clock.withZone(NAIROBI)).format(DARAJA_TIMESTAMP);
    }

    /** Equity/Jenga telco names: "Safaricom" or "Equitel". */
    public static String jengaTelco(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("Telco is required (Safaricom or Equitel)");
        }
        String t = raw.trim().toLowerCase();
        if (t.contains("equitel")) return "Equitel";
        if (t.contains("safaricom") || t.contains("mpesa") || t.contains("m-pesa")) return "Safaricom";
        throw new IllegalArgumentException("Telco must be Safaricom or Equitel");
    }

    /** Equity/Jenga wallet names for send-to-mobile ("Mpesa" per the Jenga docs). */
    public static String jengaWallet(String raw) {
        if (raw == null || raw.isBlank()) return "Mpesa";
        String w = raw.trim().toLowerCase().replace("-", "").replace(" ", "");
        if (w.contains("mpesa") || w.contains("safaricom")) return "Mpesa";
        if (w.contains("airtel")) return "Airtel";
        if (w.contains("equitel")) return "Equitel";
        return raw.trim();
    }
}
