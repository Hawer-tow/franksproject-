package com.example.MobileDb.service;

import com.example.MobileDb.security.KeyLoader;
import com.example.MobileDb.security.PublicKeyLoader;
import com.example.MobileDb.security.SignatureUtil;
import com.example.MobileDb.service.dto.JengaSendMoneyRequest;
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
public class JengaSendMoneyService {

    private static final Logger logger = Logger.getLogger(JengaSendMoneyService.class.getName());

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

    private String generateSignature(JengaSendMoneyRequest request) {
        try {
        	 String data = request.getTransfer().getAmount().trim()
        	            + request.getTransfer().getCurrencyCode().trim()
        	            + request.getTransfer().getReference().trim()
        	            + request.getSource().getAccountNumber().trim();

            logger.info("SendMoney Signature String: " + data);
            return SignatureUtil.generateSignature(data, privateKey);
        } catch (Exception e) {
            throw new RuntimeException("Error generating signature", e);
        }
    }

    public String sendMoney(JengaSendMoneyRequest request) {
        String accessToken = getAccessToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Api-Key", apiKey);

        String signature = generateSignature(request);
        headers.set("Signature", signature);

        HttpEntity<JengaSendMoneyRequest> entity = new HttpEntity<>(request, headers);
        String url = baseUrl + "/v3-apis/transaction-api/v3.0/remittance/sendmobile";

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            return response.getBody();
        } catch (HttpClientErrorException e) {
            logger.severe("SendMoney Error: " + e.getResponseBodyAsString());
            throw e;
        }
    }
}
