package com.example.MobileDb.payments;

/** A payment provider (Safaricom or Equity) rejected or could not process a request. */
public class ProviderException extends RuntimeException {

    private final String providerBody;

    public ProviderException(String message, String providerBody, Throwable cause) {
        super(message, cause);
        this.providerBody = providerBody;
    }

    public ProviderException(String message, String providerBody) {
        this(message, providerBody, null);
    }

    /** Raw response body from the provider, when there was one. */
    public String getProviderBody() {
        return providerBody;
    }
}
