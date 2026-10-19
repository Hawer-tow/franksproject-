package com.example.MobileDb.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.example.MobileDb.security.KeyLoader;
import com.example.MobileDb.security.PublicKeyLoader;
import com.example.MobileDb.security.SignatureUtil;
import com.example.MobileDb.service.dto.EquityStkPushRequest;

import jakarta.annotation.PostConstruct;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.Instant;
import java.util.logging.Logger;

import org.json.JSONObject;

@Service
public class JengaStkPushService {
	 private static final Logger logger = Logger.getLogger(JengaStkPushService.class.getName());

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
	            if (privateKeyPath.startsWith("classpath:")) {
	                privateKey = KeyLoader.loadPrivateKeyFromClasspath(privateKeyPath.replace("classpath:", ""));
	            } else {
	                privateKey = KeyLoader.loadPrivateKey(privateKeyPath);
	            }
	            if (publicKeyPath.startsWith("classpath:")) {
	                publicKey = PublicKeyLoader.loadPublicKeyFromClasspath(publicKeyPath.replace("classpath:", ""));
	            } else {
	                publicKey = PublicKeyLoader.loadPublicKey(publicKeyPath);
	            }
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


	    private String generateSignature(EquityStkPushRequest request) {
	        try {
	            // Ensure no nulls and clean concatenation
	            // Order: accountNumber + ref + mobileNumber + telco + amount + currency
	            StringBuilder sb = new StringBuilder();
	            sb.append(request.getMerchant().getAccountNumber().trim());
	            sb.append(request.getPayment().getRef().trim());
	            sb.append(request.getPayment().getMobileNumber().trim());
	            sb.append(request.getPayment().getTelco().trim());
	            sb.append(request.getPayment().getAmount().trim());
	            sb.append(request.getPayment().getCurrency().trim());

	            String data = sb.toString();
	            logger.info("Final Clean Signature String: " + data);
	            
	            return SignatureUtil.generateSignature(data, privateKey);
	        } catch (Exception e) {
	            throw new RuntimeException("Error generating signature", e);
	        }
	    }

	    public String initiateStkPush(EquityStkPushRequest request) {
	        String accessToken = getAccessToken();

	        HttpHeaders headers = new HttpHeaders();
	        headers.setBearerAuth(accessToken);
	        headers.setContentType(MediaType.APPLICATION_JSON);
	        
	        // Use the EXACT same apiKey variable as used in getAccessToken()
	        headers.set("Api-Key", this.apiKey);

	        String signature = generateSignature(request);
	        headers.set("Signature", signature);

	        logger.info("Sending Request to Jenga with Headers: " + headers);

	        HttpEntity<EquityStkPushRequest> entity = new HttpEntity<>(request, headers);
	        // Ensure you are using the UAT URL correctly
	        String url = baseUrl + "/v3-apis/payment-api/v3.0/stkussdpush/initiate";

	        try {
	            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
	            return response.getBody();
	        } catch (HttpClientErrorException e) {
	            logger.severe("401 Check - Is Token Valid? Headers: " + e.getResponseHeaders());
	            logger.severe("401 Check - Body content: " + e.getResponseBodyAsString());
	            throw e;
	        }
	    }
}
