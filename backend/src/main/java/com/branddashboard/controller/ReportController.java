package com.branddashboard.controller;

import com.branddashboard.dto.RedashEmbedUrlResponse;
import com.branddashboard.model.User;
import com.branddashboard.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/api/brands/{brandId}/reports")
public class ReportController {

    @Autowired
    private UserRepository userRepository;

    @Value("${redash.api_key}")
    private String redashApiKey;

    @Value("${redash.url}")
    private String redashUrl;

    @Value("${redash.dashboard.id}")
    private String redashDashboardId;

    @GetMapping("/redash-iframe")
    public ResponseEntity<RedashEmbedUrlResponse> getRedashIframe(@PathVariable String brandId) {
        User user = getCurrentUser();
        if (user.getBrandIds().contains(brandId)) {
            try {
                String url = generateRedashUrl(brandId);
                return ResponseEntity.ok(new RedashEmbedUrlResponse(url));
            } catch (NoSuchAlgorithmException | InvalidKeyException e) {
                // In a real app, you'd want to log this error
                return ResponseEntity.status(500).build();
            }
        } else {
            return ResponseEntity.status(403).build(); // Forbidden
        }
    }

    private String generateRedashUrl(String brandId) throws NoSuchAlgorithmException, InvalidKeyException {
        // The path for the Redash dashboard, including the parameter for the brand ID
        String queryPath = String.format("/embed/dashboards/%s", redashDashboardId);
        long expires = Instant.now().getEpochSecond() + 300; // 5-minute expiration
        
        // Redash expects the signature to be calculated on: path + expires + p_brand_id
        String toSign = queryPath + expires + brandId;

        // Create the HMAC-SHA256 signature
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(redashApiKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] signatureBytes = mac.doFinal(toSign.getBytes(StandardCharsets.UTF_8));
        String signature = Base64.getEncoder().encodeToString(signatureBytes);

        // Construct the final signed URL with parameters in the correct order
        String finalUrl = String.format("%s%s?expires=%d&p_brand_id=%s&signature=%s", 
            redashUrl, queryPath, expires, brandId, signature);
        
        // Log the generated URL for debugging
        System.out.println("Generated Redash URL: " + finalUrl);
        System.out.println("Redash URL from config: " + redashUrl);
        System.out.println("Dashboard ID from config: " + redashDashboardId);
        System.out.println("String to sign: " + toSign);
        
        return finalUrl;
    }

    private User getCurrentUser() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email = userDetails.getUsername();
        return userRepository.findByEmailId(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));
    }
}
