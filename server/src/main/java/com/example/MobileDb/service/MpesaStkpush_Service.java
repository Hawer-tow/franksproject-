package com.example.MobileDb.service;

import com.example.MobileDb.config.DarajaProperties;
import com.example.MobileDb.service.dto.mpesa2phoneStkpush_Request;
import com.example.MobileDb.service.dto.mpesa2phoneStkpush_Response;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class MpesaStkpush_Service {

    private final DarajaProperties darajaProperties;
    private final RestTemplate restTemplate;

    public MpesaStkpush_Service(DarajaProperties darajaProperties) {
        this.darajaProperties = darajaProperties;
        this.restTemplate = new RestTemplate();
    }

    public mpesa2phoneStkpush_Response initiateStkPush(mpesa2phoneStkpush_Request request) {
        String token = getAccessToken();

        String timestamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String rawPassword = darajaProperties.getShortcode() + darajaProperties.getPasskey() + timestamp;
        String password = Base64.getEncoder().encodeToString(rawPassword.getBytes(StandardCharsets.UTF_8));

        Map<String, Object> payload = new HashMap<>();
        payload.put("BusinessShortCode", darajaProperties.getShortcode());
        payload.put("Password", password);
        payload.put("Timestamp", timestamp);
        payload.put("TransactionType", "CustomerPayBillOnline");
        payload.put("Amount", request.getAmount());
        payload.put("PartyA", request.getPhoneNumber());
        payload.put("PartyB", darajaProperties.getShortcode());
        payload.put("PhoneNumber", request.getPhoneNumber());
        payload.put("CallBackURL", darajaProperties.getStkResultUrl());
        payload.put("AccountReference", "BuyingTxn");
        payload.put("TransactionDesc", "Mpesa STK Push");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

        try {
            ResponseEntity<mpesa2phoneStkpush_Response> response = restTemplate.postForEntity(
                    darajaProperties.getStkPushUrl(), // use config instead of hardcoded URL
                    entity,
                    mpesa2phoneStkpush_Response.class
            );
            
            return response.getBody();
        } catch (Exception ex) {
            throw new RuntimeException("Error initiating STK Push: " + ex.getMessage(), ex);
        }
    }

    private String getAccessToken() {
        String auth = darajaProperties.getConsumerKey() + ":" + darajaProperties.getConsumerSecret();
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Basic " + encodedAuth);

        ResponseEntity<Map> response = restTemplate.exchange(
                darajaProperties.getTokenUrl(),
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Map.class
        );

        return (String) response.getBody().get("access_token");
    }
}
