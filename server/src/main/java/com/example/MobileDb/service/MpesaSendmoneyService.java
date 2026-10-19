package com.example.MobileDb.service;

import com.example.MobileDb.config.DarajaProperties;
import com.example.MobileDb.security.SecurityUtils;
import com.example.MobileDb.service.dto.PaymentRequest;
import com.example.MobileDb.service.dto.PaymentResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.*;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
public class MpesaSendmoneyService {

    private static final Logger log = LoggerFactory.getLogger(MpesaSendmoneyService.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final DarajaProperties props;

    public MpesaSendmoneyService(DarajaProperties props) {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
        this.props = props;
    }

    public PaymentResponse sendB2C(PaymentRequest req) {
        validateRequest(req);

        String token = obtainAccessToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Generate credential dynamically
        String credential;
        try {
            credential = SecurityUtils.generateSecurityCredential(
                    props.getInitiatorPassword(),
                    props.getCertificatePath()
            );
        } catch (Exception e) {
            log.error("Failed to generate security credential", e);
            throw new RuntimeException("Failed to generate security credential: " + e.getMessage(), e);
        }

        // Build payload
        Map<String, Object> body = new HashMap<>();
        if (req.getOriginatorConversationID() != null && !req.getOriginatorConversationID().isBlank()) {
            body.put("OriginatorConversationID", req.getOriginatorConversationID());
        }
        body.put("InitiatorName", req.getInitiatorName());
        body.put("SecurityCredential", credential);
        body.put("CommandID", req.getCommandID());
        body.put("Amount", req.getAmount().intValue());
        body.put("PartyA", req.getPartyA());
        body.put("PartyB", req.getPartyB());
        body.put("Remarks", req.getRemarks());
        body.put("QueueTimeOutURL", props.getB2cTimeoutUrl()); // use B2C-specific timeout URL
        body.put("ResultURL", props.getB2cResultUrl());        // use B2C-specific result URL
        body.put("Occasion", req.getOccasion());

        try {
            String jsonBody = objectMapper.writeValueAsString(body);
            HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);

            log.info("Outgoing Daraja B2C payload: {}", jsonBody);

            ResponseEntity<String> response = restTemplate.exchange(props.getB2cUrl(), HttpMethod.POST, entity, String.class);

            log.info("Daraja raw response: status={} body={}", response.getStatusCodeValue(), response.getBody());

            Map<String, Object> raw = objectMapper.readValue(response.getBody(), new TypeReference<>() {});

            // Tag PaymentResponse with SaleType = MPESA
            PaymentResponse paymentResponse = new PaymentResponse(response.getStatusCodeValue(), "OK", raw);
            paymentResponse.setSaleType("MPESA");
            return paymentResponse;
        } catch (HttpClientErrorException | HttpServerErrorException httpEx) {
            String respBody = httpEx.getResponseBodyAsString();
            log.error("Daraja HTTP error {} body {}", httpEx.getStatusCode(), respBody, httpEx);
            try {
                Map<String, Object> raw = objectMapper.readValue(respBody, new TypeReference<>() {});
                PaymentResponse paymentResponse = new PaymentResponse(httpEx.getStatusCode().value(), "Daraja error", raw);
                paymentResponse.setSaleType("MPESA");
                return paymentResponse;
            } catch (Exception e) {
                return new PaymentResponse(httpEx.getStatusCode().value(), "Daraja error", Map.of("body", respBody));
            }
        } catch (ResourceAccessException rae) {
            log.error("Network/SSL error calling Daraja: {}", rae.getMessage(), rae);
            throw new RuntimeException("Network error calling Daraja: " + rae.getMessage(), rae);
        } catch (Exception ex) {
            log.error("Unexpected error sending B2C: {}", ex.getMessage(), ex);
            throw new RuntimeException("Unexpected error sending B2C: " + ex.getMessage(), ex);
        }
    }

    private void validateRequest(PaymentRequest req) {
        if (req.getInitiatorName() == null || req.getInitiatorName().isBlank())
            throw new IllegalArgumentException("InitiatorName is required");
        if (req.getAmount() == null || req.getAmount() <= 0)
            throw new IllegalArgumentException("Amount must be > 0");
        if (req.getPartyA() == null || req.getPartyA().isBlank())
            throw new IllegalArgumentException("PartyA (shortcode) is required");
        if (req.getPartyB() == null || req.getPartyB().isBlank())
            throw new IllegalArgumentException("PartyB (recipient) is required");
    }

    private String obtainAccessToken() {
        try {
            String creds = props.getConsumerKey() + ":" + props.getConsumerSecret();
            String encoded = Base64.getEncoder().encodeToString(creds.getBytes(StandardCharsets.UTF_8));

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Basic " + encoded);

            HttpEntity<String> entity = new HttpEntity<>("", headers);

            ResponseEntity<String> response = restTemplate.exchange(props.getTokenUrl(), HttpMethod.GET, entity, String.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                log.error("Failed to obtain token, status: {}, body: {}", response.getStatusCodeValue(), response.getBody());
                throw new RuntimeException("Failed to obtain access token");
            }

            Map<String, Object> map = objectMapper.readValue(response.getBody(), new TypeReference<>() {});
            Object tokenObj = map.get("access_token");
            if (tokenObj == null) throw new RuntimeException("access_token not present in token response");

            log.info("Obtained Daraja access token successfully");
            return tokenObj.toString();
        } catch (Exception ex) {
            log.error("Error obtaining access token: {}", ex.getMessage(), ex);
            throw new RuntimeException("Error obtaining access token: " + ex.getMessage(), ex);
        }
    }
}
