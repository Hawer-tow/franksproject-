package com.example.MobileDb.service;

import com.example.MobileDb.config.DarajaProperties;
import com.example.MobileDb.payments.DarajaCallbacks;
import com.example.MobileDb.payments.PaymentFormat;
import com.example.MobileDb.payments.ProviderException;
import com.example.MobileDb.service.dto.mpesa2phoneStkpush_Response;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Lipa na M-Pesa Online (STK push): sends a payment prompt to the customer's phone, and queries
 * the result when the callback did not arrive.
 *
 * Daraja rules applied here: whole-shilling Amount, PhoneNumber/PartyA as 2547XXXXXXXX,
 * Password = Base64(ShortCode + Passkey + Timestamp) with Timestamp yyyyMMddHHmmss (Kenyan time),
 * AccountReference at most 12 characters, TransactionDesc at most 13.
 */
@Service
public class MpesaStkpush_Service {

    private static final Logger log = LoggerFactory.getLogger(MpesaStkpush_Service.class);

    private final DarajaProperties props;
    private final DarajaAuthService auth;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper json = new ObjectMapper();
    private final Clock clock = Clock.systemUTC();

    public MpesaStkpush_Service(DarajaProperties props, DarajaAuthService auth) {
        this.props = props;
        this.auth = auth;
    }

    /**
     * Sends the prompt. Returns Safaricom's acknowledgement (ResponseCode "0" = prompt sent);
     * the payment result arrives later on the callback URL.
     */
    public mpesa2phoneStkpush_Response initiate(String msisdn, int amount, String accountReference, String description) {
        String timestamp = PaymentFormat.darajaTimestamp(clock);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("BusinessShortCode", props.getShortcode());
        payload.put("Password", password(timestamp));
        payload.put("Timestamp", timestamp);
        payload.put("TransactionType", "CustomerPayBillOnline");
        payload.put("Amount", amount);
        payload.put("PartyA", msisdn);
        payload.put("PartyB", props.getShortcode());
        payload.put("PhoneNumber", msisdn);
        payload.put("CallBackURL", props.getStkResultUrl());
        payload.put("AccountReference", PaymentFormat.limit(accountReference, 12, "FrankMpesa"));
        payload.put("TransactionDesc", PaymentFormat.limit(description, 13, "Payment"));

        String body = post(props.getStkPushUrl(), payload, "send the M-Pesa prompt");
        try {
            mpesa2phoneStkpush_Response response = json.readValue(body, mpesa2phoneStkpush_Response.class);
            if (!"0".equals(response.getResponseCode())) {
                throw new ProviderException("Safaricom did not send the prompt: " + response.getResponseDescription(), body);
            }
            return response;
        } catch (ProviderException e) {
            throw e;
        } catch (Exception e) {
            throw new ProviderException("Unexpected reply from Safaricom", body, e);
        }
    }

    /**
     * STK push query: asks Safaricom for the result of a prompt. Returns the parsed result, or null
     * while the customer has not answered yet ("The transaction is being processed").
     */
    public DarajaCallbacks.StkResult query(String checkoutRequestId) {
        String timestamp = PaymentFormat.darajaTimestamp(clock);
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("BusinessShortCode", props.getShortcode());
        payload.put("Password", password(timestamp));
        payload.put("Timestamp", timestamp);
        payload.put("CheckoutRequestID", checkoutRequestId);

        String url = props.getStkPushUrl().replace("/stkpush/v1/processrequest", "/stkpushquery/v1/query");
        String body;
        try {
            body = post(url, payload, "check the M-Pesa prompt");
        } catch (ProviderException e) {
            // Safaricom answers "being processed" with an error status while the customer decides
            if (e.getProviderBody() != null && e.getProviderBody().contains("being processed")) {
                return null;
            }
            throw e;
        }
        Map<String, Object> map = readMap(body);
        Object resultCode = map.get("ResultCode");
        if (resultCode == null) return null;
        return new DarajaCallbacks.StkResult(
                str(map.get("MerchantRequestID")), str(map.get("CheckoutRequestID")),
                Integer.parseInt(resultCode.toString().trim()), str(map.get("ResultDesc")),
                null, null, null);
    }

    private String password(String timestamp) {
        String raw = props.getShortcode() + props.getPasskey() + timestamp;
        return Base64.getEncoder().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    private String post(String url, Map<String, Object> payload, String action) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(auth.accessToken());
        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.POST, new HttpEntity<>(payload, headers), String.class);
            return response.getBody();
        } catch (HttpStatusCodeException e) {
            String body = e.getResponseBodyAsString();
            if (e.getStatusCode().value() == 401) auth.invalidate();
            Map<String, Object> err = readMap(body);
            String msg = str(err.getOrDefault("errorMessage", err.getOrDefault("ResponseDescription", e.getStatusText())));
            log.warn("Daraja refused to {}: HTTP {} {}", action, e.getStatusCode().value(), body);
            throw new ProviderException("Safaricom could not " + action + ": " + msg, body, e);
        } catch (RestClientException e) {
            throw new ProviderException("Could not reach Safaricom to " + action + ": " + e.getMessage(), null, e);
        }
    }

    private Map<String, Object> readMap(String body) {
        try {
            return body == null ? Map.of() : json.readValue(body, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            return Map.of();
        }
    }

    private static String str(Object o) {
        return o == null ? null : o.toString();
    }
}
