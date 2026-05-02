package com.auth.service;

import java.time.LocalDateTime;
import java.util.Random;

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
    @Autowired
    private EmailService emailService;

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
    // Password Reset Functionality
    public String generateAndSendOtp(String email) {
        // 1. Find user in MySQL
        UserCredential user = repository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        // 2. Generate a 6-digit numeric OTP
        String otp = String.valueOf(new Random().nextInt(900000) + 100000);
        
        // 3. Save OTP and Expiry (15 mins) to DB
        user.setResetToken(otp);
        user.setTokenExpiry(LocalDateTime.now().plusMinutes(15));
        repository.save(user);

        // 4. Send the real email
        emailService.sendOtpEmail(email, otp);
        
        return "OTP sent to " + email;
    }
    // Validate OTP and Reset Password
    public String validateOtpAndResetPassword(String otp, String newPassword) {
        // 1. Find user by the OTP code
        UserCredential user = repository.findByResetToken(otp)
                .orElseThrow(() -> new RuntimeException("Invalid OTP"));

        // 2. Check if OTP is still valid
        if (user.getTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("OTP has expired. Please request a new one.");
        }

        // 3. Update with BCrypt encoded password
        user.setPassword(passwordEncoder.encode(newPassword));
        
        // 4. Clear OTP fields so they can't be used again
        user.setResetToken(null);
        user.setTokenExpiry(null);
        repository.save(user);

        return "Password updated successfully!";
    }
     
}