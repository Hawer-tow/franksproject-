package com.example.MobileDb.security;

import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.util.Base64;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.spec.X509EncodedKeySpec;
import java.security.KeyFactory;

public class SignatureTest {

    public static void main(String[] args) throws Exception {
        // Load private key
        PrivateKey privateKey = KeyLoader.loadPrivateKey("src/main/resources/keys/jenga_private.pem");

        // Load public key
        String pubKeyPem = new String(Files.readAllBytes(Paths.get("src/main/resources/keys/jenga_public.pem")));
        pubKeyPem = pubKeyPem.replace("-----BEGIN PUBLIC KEY-----", "")
                             .replace("-----END PUBLIC KEY-----", "")
                             .replaceAll("\\s", "");
        byte[] decoded = Base64.getDecoder().decode(pubKeyPem);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);
        PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(spec);

        // Test data string (same concatenation as in your service)
        String data = "0170194290581" + "TXN12345" + "2547XXXXXXXX" + "Safaricom" + "500" + "KES";

        // Sign
        Signature sig = Signature.getInstance("SHA256withRSA");
        sig.initSign(privateKey);
        sig.update(data.getBytes(StandardCharsets.UTF_8));
        String signatureBase64 = Base64.getEncoder().encodeToString(sig.sign());

        // Verify
        Signature verifier = Signature.getInstance("SHA256withRSA");
        verifier.initVerify(publicKey);
        verifier.update(data.getBytes(StandardCharsets.UTF_8));
        boolean isValid = verifier.verify(Base64.getDecoder().decode(signatureBase64));

        System.out.println("Signature valid? " + isValid);
    }
}
