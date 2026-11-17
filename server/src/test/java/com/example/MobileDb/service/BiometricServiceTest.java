package com.example.MobileDb.service;

import com.example.MobileDb.entity.kisenyi.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Signature;
import java.security.spec.ECGenParameterSpec;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

/** Mirrors what the phone does: EC P-256 key in the Keystore, SHA256withECDSA over the login message. */
class BiometricServiceTest {

    private static final long NOW = Instant.parse("2026-11-17T10:00:00Z").toEpochMilli();

    private KeyPair phoneKey;
    private BiometricService service;
    private User user;

    @BeforeEach
    void setUp() throws Exception {
        KeyPairGenerator gen = KeyPairGenerator.getInstance("EC");
        gen.initialize(new ECGenParameterSpec("secp256r1"));
        phoneKey = gen.generateKeyPair();

        service = new BiometricService(Clock.fixed(Instant.ofEpochMilli(NOW), ZoneOffset.UTC));
        user = new User();
        user.setId(7);
        user.setUsername("tenet");
        user.setBiometricPublicKey(service.normalisePublicKey(
                Base64.getEncoder().encodeToString(phoneKey.getPublic().getEncoded())));
    }

    private String sign(String deviceId, long timestamp) throws Exception {
        Signature s = Signature.getInstance("SHA256withECDSA");
        s.initSign(phoneKey.getPrivate());
        s.update(BiometricService.loginMessage("tenet", deviceId, timestamp).getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(s.sign());
    }

    @Test
    void acceptsAFreshSignatureOnce() throws Exception {
        long ts = NOW - 2_000;
        String sig = sign("phone-1", ts);
        assertTrue(service.verifyLogin(user, "phone-1", ts, sig));
        assertFalse(service.verifyLogin(user, "phone-1", ts, sig), "the same signature must not work twice");
    }

    @Test
    void rejectsOldOrFutureTimestamps() throws Exception {
        long old = NOW - BiometricService.MAX_CLOCK_SKEW_MS - 1_000;
        assertFalse(service.verifyLogin(user, "phone-1", old, sign("phone-1", old)));
        long future = NOW + BiometricService.MAX_CLOCK_SKEW_MS + 1_000;
        assertFalse(service.verifyLogin(user, "phone-1", future, sign("phone-1", future)));
    }

    @Test
    void rejectsSignatureForAnotherDeviceOrUser() throws Exception {
        long ts = NOW;
        String sig = sign("phone-1", ts);
        assertFalse(service.verifyLogin(user, "phone-2", ts, sig));
        user.setUsername("someone-else");
        assertFalse(service.verifyLogin(user, "phone-1", ts, sig));
    }

    @Test
    void rejectsMissingOrGarbageInput() {
        assertFalse(service.verifyLogin(user, "phone-1", NOW, "not-base64!"));
        assertFalse(service.verifyLogin(user, "phone-1", null, "abc"));
        user.setBiometricPublicKey(null);
        assertFalse(service.verifyLogin(user, "phone-1", NOW, "abc"));
        assertThrows(IllegalArgumentException.class, () -> service.normalisePublicKey("garbage"));
    }

    @Test
    void stillAcceptsRsaKeys() throws Exception {
        KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
        gen.initialize(2048);
        KeyPair rsa = gen.generateKeyPair();
        user.setBiometricPublicKey(Base64.getEncoder().encodeToString(rsa.getPublic().getEncoded()));
        Signature s = Signature.getInstance("SHA256withRSA");
        s.initSign(rsa.getPrivate());
        s.update(BiometricService.loginMessage("tenet", "phone-1", NOW).getBytes(StandardCharsets.UTF_8));
        assertTrue(service.verifyLogin(user, "phone-1", NOW, Base64.getEncoder().encodeToString(s.sign())));
    }
}
