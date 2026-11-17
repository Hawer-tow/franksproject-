package com.example.MobileDb.payments;

/** Status values stored on provider transactions and mirrored onto sales. */
public final class PaymentStatus {
    public static final String PENDING = "PENDING";
    public static final String SUCCESS = "SUCCESS";
    public static final String FAILED = "FAILED";
    public static final String CANCELLED = "CANCELLED";
    public static final String TIMEOUT = "TIMEOUT";

    private PaymentStatus() {}

    public static boolean isFinal(String status) {
        return status != null && !PENDING.equals(status);
    }
}
