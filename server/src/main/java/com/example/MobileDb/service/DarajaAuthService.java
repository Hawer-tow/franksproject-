package com.example.MobileDb.service;

import com.example.MobileDb.config.DarajaProperties;
import com.example.MobileDb.payments.ProviderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;

/**
 * Daraja OAuth token, cached until shortly before it expires (tokens last about an hour),
 * instead of requesting a new one for every payment.
 */
@Service
public class DarajaAuthService {

    private static final Logger log = LoggerFactory.getLogger(DarajaAuthService.class);

    private final DarajaProperties props;
    private final RestTemplate restTemplate = new RestTemplate();

    private String cachedToken;
    private Instant expiresAt = Instant.EPOCH;

    public DarajaAuthService(DarajaProperties props) {
        this.props = props;
    }

    public synchronized String accessToken() {
        if (cachedToken != null && Instant.now().isBefore(expiresAt)) {
            return cachedToken;
        }
        String creds = props.getConsumerKey() + ":" + props.getConsumerSecret();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Basic " + Base64.getEncoder().encodeToString(creds.getBytes(StandardCharsets.UTF_8)));
        try {
            @SuppressWarnings("rawtypes")
            ResponseEntity<Map> response = restTemplate.exchange(
                    props.getTokenUrl(), HttpMethod.GET, new HttpEntity<>(headers), Map.class);
            Map<?, ?> body = response.getBody();
            Object token = body != null ? body.get("access_token") : null;
            if (token == null) {
                throw new ProviderException("Safaricom did not return an access token", String.valueOf(body));
            }
            long seconds = 3599;
            Object expiresIn = body.get("expires_in");
            if (expiresIn != null) {
                try {
                    seconds = Long.parseLong(expiresIn.toString().trim());
                } catch (NumberFormatException ignored) {
                    // keep the default
                }
            }
            cachedToken = token.toString();
            expiresAt = Instant.now().plusSeconds(Math.max(60, seconds - 60));
            log.info("Obtained Daraja access token (valid ~{} s)", seconds);
            return cachedToken;
        } catch (RestClientException e) {
            throw new ProviderException("Could not log in to Safaricom Daraja: " + e.getMessage(), null, e);
        }
    }

    /** Drop the cached token, e.g. after Safaricom rejects it. */
    public synchronized void invalidate() {
        cachedToken = null;
        expiresAt = Instant.EPOCH;
    }
}
