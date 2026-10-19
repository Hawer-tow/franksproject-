package com.example.MobileDb.security;

import com.example.MobileDb.repository.kisenyi.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.ReadListener;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.util.Base64;
import java.util.stream.Collectors;

public class UserAccessFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;
    private final PrivateKey serverPrivateKey;

    public UserAccessFilter(UserRepository userRepository,
                            PrivateKey serverPrivateKey) {
        this.userRepository = userRepository;
        this.serverPrivateKey = serverPrivateKey;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        // ✅ Exclude only truly public endpoints
        boolean isExcludedEndpoint = path.startsWith("/api/public-key")
        	    || (path.startsWith("/api/users") && "GET".equalsIgnoreCase(request.getMethod()));


        if (isExcludedEndpoint) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
         
        	// Step 1: Get encryption headers
        	String encryptedKey = request.getHeader("X-Encrypted-Key");
        	String encryptedPayload = request.getHeader("X-Encrypted-Payload");
        	String iv = request.getHeader("X-IV");

        	System.out.println("=== Incoming Request Headers ===");
        	System.out.println("X-Encrypted-Key: " + encryptedKey);
        	System.out.println("X-Encrypted-Payload: " + encryptedPayload);
        	System.out.println("X-IV: " + iv);

        	if (encryptedKey == null || encryptedPayload == null || iv == null) {
        	    writeJsonError(response, HttpServletResponse.SC_BAD_REQUEST,
        	            900101, "Missing encryption headers");
        	    return;
        	}

        	// Step 2: Decrypt AES key
        	SecretKey aesKey = CryptoUtils.decryptAESKey(encryptedKey, serverPrivateKey);
        	System.out.println("AES key decrypted successfully: " +
        	        Base64.getEncoder().encodeToString(aesKey.getEncoded()));

        	// Step 3: Decrypt payload
        	String decryptedPayload = CryptoUtils.decryptPayload(encryptedPayload, iv, aesKey);
        	System.out.println("Decrypted payload: " + decryptedPayload);

        	// Step 4: Wrap request with decrypted JSON body
        	HttpServletRequest wrappedRequest = new DecryptedHttpServletRequestWrapper(request, decryptedPayload);

        	// Step 5: Continue with user access checks
        	String userIdHeader = request.getHeader("X-User-Id");
        	System.out.println("X-User-Id: " + userIdHeader);

        	if (userIdHeader == null) {
        	    writeJsonError(response, HttpServletResponse.SC_UNAUTHORIZED,
        	            40101, "Missing user ID");
        	    return;
        	}

        	int userId = Integer.parseInt(userIdHeader);
        	var userOpt = userRepository.findById(userId);

        	if (!Boolean.TRUE.equals(userOpt.get().getIsAdmin())) {
        	    if (userOpt.isEmpty() || userOpt.get().getAccessStatus() != 1) {
        	        writeJsonError(response, HttpServletResponse.SC_FORBIDDEN,
        	                40301, "Access denied: inactive user");
        	        return;
        	    }
        	}

        	System.out.println("User access check passed for userId=" + userId);

        	// ✅ Only ONE call here
        	filterChain.doFilter(wrappedRequest, response);


        } catch (Exception e) {
            e.printStackTrace();
            writeJsonError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    50001, "Encryption validation failed: " + e.getMessage());
        }
    }

    private void writeJsonError(HttpServletResponse response,
                                int httpStatus,
                                int code,
                                String message) throws IOException {
        response.setStatus(httpStatus);
        response.setContentType("application/json");
        String json = String.format(
                "{\"status\":false,\"code\":%d,\"message\":\"%s\"}",
                code, message
        );
        response.getWriter().write(json);
    }

    // ✅ Wrapper class to inject decrypted JSON into request body
    private static class DecryptedHttpServletRequestWrapper extends HttpServletRequestWrapper {
        private final byte[] body;

        public DecryptedHttpServletRequestWrapper(HttpServletRequest request, String decryptedJson) {
            super(request);
            this.body = decryptedJson.getBytes(StandardCharsets.UTF_8);
        }

        @Override
        public ServletInputStream getInputStream() {
            ByteArrayInputStream bais = new ByteArrayInputStream(body);
            return new ServletInputStream() {
                @Override public int read() { return bais.read(); }
                @Override public boolean isFinished() { return bais.available() == 0; }
                @Override public boolean isReady() { return true; }
                @Override public void setReadListener(ReadListener readListener) {}
            };
        }

        @Override
        public BufferedReader getReader() {
            return new BufferedReader(new InputStreamReader(getInputStream(), StandardCharsets.UTF_8));
        }
    }
}
