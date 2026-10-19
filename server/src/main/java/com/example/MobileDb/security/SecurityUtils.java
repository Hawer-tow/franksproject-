package com.example.MobileDb.security;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.FileSystemResource;

import javax.crypto.Cipher;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.security.Security;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;

public class SecurityUtils {

    private static final String RSA_TRANSFORMATION = "RSA/ECB/PKCS1Padding";
    private static final String BC_PROVIDER = "BC";

    static {
        // Add BouncyCastle provider once
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    /**
     * Generate a Base64-encoded security credential by encrypting the initiator password
     * with the public key from the provided X.509 certificate.
     *
     * certificatePath may be:
     *  - a classpath-relative path (e.g. "certs/SandboxCertificate.cer")
     *  - an absolute filesystem path (e.g. "C:/path/to/SandboxCertificate.cer")
     *
     * @param initiatorPassword plain-text initiator password (e.g. "Safaricom123!!")
     * @param certificatePath classpath or absolute path to the .cer file
     * @return single-line Base64 encoded encrypted credential
     * @throws Exception on any IO or crypto error
     */
    public static String generateSecurityCredential(String initiatorPassword, String certificatePath) throws Exception {
        if (initiatorPassword == null || initiatorPassword.isBlank()) {
            throw new IllegalArgumentException("initiatorPassword must not be null or blank");
        }
        if (certificatePath == null || certificatePath.isBlank()) {
            throw new IllegalArgumentException("certificatePath must not be null or blank");
        }

        Resource resource = locateResource(certificatePath);

        try (InputStream inputStream = resource.getInputStream()) {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            X509Certificate certificate = (X509Certificate) cf.generateCertificate(inputStream);
            PublicKey pk = certificate.getPublicKey();

            Cipher cipher = Cipher.getInstance(RSA_TRANSFORMATION, BC_PROVIDER);
            cipher.init(Cipher.ENCRYPT_MODE, pk);

            byte[] plain = initiatorPassword.getBytes(StandardCharsets.UTF_8);
            byte[] cipherText = cipher.doFinal(plain);

            // Return single-line Base64 string
            return Base64.getEncoder().encodeToString(cipherText).trim();
        }
    }

    /**
     * Try to locate the certificate as a classpath resource first, then as a filesystem path.
     */
    private static Resource locateResource(String certificatePath) {
        // Try classpath
        ClassPathResource classPathResource = new ClassPathResource(certificatePath);
        if (classPathResource.exists()) {
            return classPathResource;
        }

        // Fallback to filesystem path
        FileSystemResource fsResource = new FileSystemResource(certificatePath);
        if (fsResource.exists()) {
            return fsResource;
        }

        // Not found
        throw new IllegalArgumentException("Certificate not found on classpath or filesystem: " + certificatePath);
    }
}
