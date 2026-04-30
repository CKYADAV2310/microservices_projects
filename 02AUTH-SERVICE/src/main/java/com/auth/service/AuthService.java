package com.auth.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.auth.entity.UserCredential;
import com.auth.repo.UserCredentialRepository;

@Service
public class AuthService {

    @Autowired
    private UserCredentialRepository repository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder; 

    /**
     * Registers a user and forces ROLE_ identification.
     */
    public String saveUser(UserCredential user) {
        // 1. Encrypt password
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        
        // 2. Force ROLE_ prefix for identification consistency
        if (user.getRole() != null && user.getRole().equalsIgnoreCase("ADMIN")) {
            user.setRole("ROLE_ADMIN");
        } else {
            user.setRole("ROLE_USER");
        }
        
        repository.save(user);
        return "User registered successfully with role: " + user.getRole();
    }

    /**
     * Generates a JWT token using the prefixed role.
     */
    public String generateToken(String email) {
        UserCredential user = repository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));
        
        // Ensure the JWT contains "ROLE_ADMIN" or "ROLE_USER"
        return jwtService.generateToken(user.getUsername(), user.getRole());
    }

    public void validateToken(String token) {
        jwtService.validateToken(token);
    }
}