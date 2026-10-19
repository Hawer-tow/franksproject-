package com.example.MobileDb.service;

import com.example.MobileDb.entity.kisenyi.Selling;
import com.example.MobileDb.service.dto.EquityStkPushRequest;
import com.example.MobileDb.security.KeyLoader;
import com.example.MobileDb.security.SignatureUtil;
import com.example.MobileDb.security.PublicKeyLoader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import org.json.JSONObject;
import org.springframework.web.client.HttpClientErrorException;

import jakarta.annotation.PostConstruct;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.Instant;
import java.util.logging.Logger;

@Service
public class JengaPaymentService {

    private static final Logger logger = Logger.getLogger(JengaPaymentService.class.getName());

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

   
}
