package com.branddashboard.controller;

import com.branddashboard.model.Brand;
import com.branddashboard.model.User;
import com.branddashboard.repository.BrandRepository;
import com.branddashboard.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/brands")
public class BrandController {

    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<Brand>> getMyBrands() {
        User user = getCurrentUser();
        List<Brand> brands = brandRepository.findByIdIn(user.getBrandIds());
        return ResponseEntity.ok(brands);
    }

    @GetMapping("/{brandId}")
    public ResponseEntity<Brand> getBrandById(@PathVariable String brandId) {
        User user = getCurrentUser();
        if (user.getBrandIds().contains(brandId)) {
            Brand brand = brandRepository.findById(brandId)
                    .orElseThrow(() -> new RuntimeException("Brand not found"));
            return ResponseEntity.ok(brand);
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
