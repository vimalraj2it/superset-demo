package com.branddashboard.controller;

import com.branddashboard.dto.MetricsSummary;
import com.branddashboard.model.User;
import com.branddashboard.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/brands/{brandId}/metrics")
public class MetricsController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/summary")
    public ResponseEntity<MetricsSummary> getMetricsSummary(@PathVariable String brandId) {
        User user = getCurrentUser();
        if (user.getBrandIds().contains(brandId)) {
            // In a real application, you would compute these metrics from your data
            MetricsSummary summary = new MetricsSummary(120, 45000.75, 375.01, 85);
            return ResponseEntity.ok(summary);
        } else {
            return ResponseEntity.status(403).build(); // Forbidden
        }
    }

    private User getCurrentUser() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email = userDetails.getUsername();
        return userRepository.findByEmailId(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
