package com.example.MobileDb.config;

import com.example.MobileDb.repository.kisenyi.UserRepository;
import com.example.MobileDb.security.UserAccessFilter;
import com.example.MobileDb.security.CryptoUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.PrivateKey;
import java.security.PublicKey;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UserRepository userRepository;

    public SecurityConfig(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // ✅ Load keys from PEM files (adjust paths to your actual key files)
       
        
        byte[] privateKeyBytes = Files.readAllBytes(Paths.get("src/main/resources/keys/mobile2serverencryption_decription_private_key.pem"));
        byte[] publicKeyBytes = Files.readAllBytes(Paths.get("src/main/resources/keys/mobile2serverencryption_decription_public_key.pem"));


        PrivateKey serverPrivateKey = CryptoUtils.loadPrivateKey(privateKeyBytes);
        PublicKey clientPublicKey = CryptoUtils.loadPublicKey(publicKeyBytes);

        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
            .addFilterBefore(
            	    new UserAccessFilter(userRepository, serverPrivateKey),
            	    org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class
            	);


        return http.build();
    }
}
