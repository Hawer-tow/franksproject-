package com.example.MobileDb.service;

import com.example.MobileDb.security.KeyLoader;
import com.example.MobileDb.security.PublicKeyLoader;
import com.example.MobileDb.security.SignatureUtil;
import com.example.MobileDb.service.dto.EquityAccountSendMoneyRequest;
import jakarta.annotation.PostConstruct;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.Instant;
import java.util.logging.Logger;

@Service
public class EquityAccountSendMoneyService {

    private static final Logger logger = Logger.getLogger(EquityAccountSendMoneyService.class.getName());

    @Value("${jenga.apiKey}")
    private String apiKey;

    @Value("${jenga.consumerSecret}")
    private String consumerSecret;

    @Value("${jenga.merchantCode}")
    private String merchantCode;

    @Value("${jenga.baseUrl}")
    private String baseUrl;

    @Value("${jenga.privateKeyPath}")
    private String privateKeyPath;

    @Value("${jenga.publicKeyPath}")
    private String publicKeyPath;

    private PrivateKey privateKey;
    private PublicKey publicKey;
    private final RestTemplate restTemplate = new RestTemplate();

    private String cachedToken;
    private Instant tokenExpiry;

    @PostConstruct
    public void init() {
        try {
            privateKey = privateKeyPath.startsWith("classpath:")
                    ? KeyLoader.loadPrivateKeyFromClasspath(privateKeyPath.replace("classpath:", ""))
                    : KeyLoader.loadPrivateKey(privateKeyPath);
            publicKey = publicKeyPath.startsWith("classpath:")
                    ? PublicKeyLoader.loadPublicKeyFromClasspath(publicKeyPath.replace("classpath:", ""))
                    : PublicKeyLoader.loadPublicKey(publicKeyPath);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load keys", e);
        }
    }

    private String getAccessToken() {
        if (cachedToken != null && tokenExpiry != null && Instant.now().isBefore(tokenExpiry)) {
            return cachedToken;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set("Api-Key", apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        JSONObject body = new JSONObject();
        body.put("merchantCode", merchantCode);
        body.put("consumerSecret", consumerSecret);

        HttpEntity<String> entity = new HttpEntity<>(body.toString(), headers);
        String url = baseUrl + "/authentication/api/v3/authenticate/merchant";

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
        if (response.getStatusCode().is2xxSuccessful()) {
            JSONObject json = new JSONObject(response.getBody());
            cachedToken = json.getString("accessToken");
            tokenExpiry = Instant.parse(json.getString("expiresIn"));
            return cachedToken;
        } else {
            throw new RuntimeException("Auth failed: " + response.getStatusCode());
        }
    }

 // Update the signature generation order for Bank Transfers
    private String generateSignature(EquityAccountSendMoneyRequest request) {
        try {
            // Concatenation Order: source.accountNumber + transfer.amount + transfer.currencyCode + transfer.reference
            String data = request.getSource().getAccountNumber().trim()
                    + request.getTransfer().getAmount().trim()
                    + request.getTransfer().getCurrencyCode().trim()
                    + request.getTransfer().getReference().trim();

            logger.info("Bank Transfer Signature String: " + data);
            return SignatureUtil.generateSignature(data, privateKey);
        } catch (Exception e) {
            throw new RuntimeException("Error generating signature", e);
        }
    }

    // Ensure the endpoint is set correctly for bank transfers
    public String sendMoney(EquityAccountSendMoneyRequest request) {
        String accessToken = getAccessToken();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Api-Key", apiKey);

        headers.set("Signature", generateSignature(request));

        HttpEntity<EquityAccountSendMoneyRequest> entity = new HttpEntity<>(request, headers);
        // Correct endpoint for Internal/Within Equity transfers
        String url = baseUrl + "/v3-apis/transaction-api/v3.0/remittance/internalBankTransfer";

        try {
            return restTemplate.exchange(url, HttpMethod.POST, entity, String.class).getBody();
        } catch (HttpClientErrorException e) {
            logger.severe("Bank Transfer Error: " + e.getResponseBodyAsString());
            throw e;
        }
    }

}
