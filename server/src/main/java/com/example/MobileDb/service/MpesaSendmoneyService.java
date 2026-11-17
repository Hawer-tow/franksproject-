package com.example.MobileDb.service;

import com.example.MobileDb.config.DarajaProperties;
import com.example.MobileDb.payments.PaymentFormat;
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
    private final DarajaAuthService auth;

    public MpesaSendmoneyService(DarajaProperties props, DarajaAuthService auth) {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
        this.props = props;
        this.auth = auth;
    }

    public PaymentResponse sendB2C(PaymentRequest req) {
        validateRequest(req);

        String token = auth.accessToken();

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
        // Our own id: Safaricom echoes it in the result callback, which is how the sale is found again
        body.put("OriginatorConversationID", req.getOriginatorConversationID());
        body.put("InitiatorName", req.getInitiatorName());
        body.put("SecurityCredential", credential);
        body.put("CommandID", req.getCommandID());
        body.put("Amount", req.getAmount().intValue());
        body.put("PartyA", req.getPartyA());
        body.put("PartyB", req.getPartyB());
        body.put("Remarks", PaymentFormat.limit(req.getRemarks(), 100, "Payment"));
        body.put("QueueTimeOutURL", props.getB2cTimeoutUrl()); // use B2C-specific timeout URL
        body.put("ResultURL", props.getB2cResultUrl());        // use B2C-specific result URL
        body.put("Occasion", PaymentFormat.limit(req.getOccasion(), 100, "Payment"));

        try {
            String jsonBody = objectMapper.writeValueAsString(body);
            HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);

            log.info("Sending Daraja B2C: amount={} to={} ref={}", body.get("Amount"), body.get("PartyB"),
                    body.get("OriginatorConversationID"));

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
        if (req.getOriginatorConversationID() == null || req.getOriginatorConversationID().isBlank())
            throw new IllegalArgumentException("OriginatorConversationID is required");
    }

}
