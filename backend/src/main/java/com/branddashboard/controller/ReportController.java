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

    @Value("${jwt.secret}")
    private String jwtSecret;

    @GetMapping("/redash-iframe")
    public ResponseEntity<RedashEmbedUrlResponse> getRedashIframe(@PathVariable String brandId) {
        User user = getCurrentUser();
        if (user.getBrandIds().contains(brandId)) {
            try {
                String url = generateRedashEmbedUrl(brandId, user);
                return ResponseEntity.ok(new RedashEmbedUrlResponse(url));
            } catch (Exception e) {
                e.printStackTrace();
                return ResponseEntity.status(500).build();
            }
        } else {
            return ResponseEntity.status(403).build(); // Forbidden
        }
    }

    private String generateRedashEmbedUrl(String brandId, User user) throws Exception {
        // Since Redash embed requires authentication, let's use the query API approach
        // This will return data that can be embedded in a custom dashboard
        
        // First, let's try to get the dashboard data using the API
        String queryUrl = String.format("%s/api/dashboards/%s?api_key=%s", 
            redashUrl, redashDashboardId, redashApiKey);
        
        // Log the generated URL for debugging
        System.out.println("Generated Redash Query URL: " + queryUrl);
        System.out.println("Redash URL from config: " + redashUrl);
        System.out.println("Dashboard ID from config: " + redashDashboardId);
        System.out.println("Using Query API approach");
        
        // For now, return the regular dashboard URL as fallback
        // In a real implementation, you'd fetch the data and create a custom embed
        return String.format("%s/dashboards/%s-test?p_brand_id=%s", 
            redashUrl, redashDashboardId, brandId);
    }

    private String generateRedashJWT(User user) {
        try {
            // Create JWT payload for Redash with the exact format it expects
            Map<String, Object> claims = new HashMap<>();
            claims.put("user_id", user.getId());
            claims.put("email", user.getEmailId());
            claims.put("name", user.getEmailId());
            claims.put("org_id", 1);
            claims.put("exp", Instant.now().getEpochSecond() + 3600); // 1 hour expiration
            claims.put("iat", Instant.now().getEpochSecond()); // issued at
            claims.put("iss", "brand-dashboard"); // issuer
            claims.put("aud", "redash"); // audience
            
            // Create JWT header
            String header = Base64.getEncoder().encodeToString("{\"alg\":\"HS256\",\"typ\":\"JWT\"}".getBytes());
            
            // Create JWT payload
            String payload = Base64.getEncoder().encodeToString(
                new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(claims).getBytes()
            );
            
            // Create signature
            String data = header + "." + payload;
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(jwtSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] signatureBytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            String signature = Base64.getEncoder().encodeToString(signatureBytes);
            
            // Return complete JWT
            return data + "." + signature;
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate JWT token", e);
        }
    }

    private User getCurrentUser() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email = userDetails.getUsername();
        return userRepository.findByEmailId(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));
    }
}
