package com.auth.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.auth.dto.AuthRequest;
import com.auth.entity.UserCredential;
import com.auth.service.AuthService;

/**
 * REST Controller for managing User Authentication and Registration.
 * Handles incoming requests for signing up and obtaining JWT tokens.
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService service;

    @Autowired
    private AuthenticationManager authenticationManager;

    /**
     * Endpoint to register a new user in the system.
     * @param user The user details (Username, Email, Password, Role).
     * @return Success message upon successful registration.
     */
    @PostMapping("/register")
    public ResponseEntity<String> addNewUser(@RequestBody UserCredential user) {
        String response = service.saveUser(user);
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint to authenticate a user and provide a JWT token.
     * Uses Email as the primary identifier for login.
     * @param authRequest Contains the user's email and password.
     * @return A signed JWT token if credentials are valid.
     * @throws RuntimeException if authentication fails.
     */
    @PostMapping("/token")
    public ResponseEntity<String> getToken(@RequestBody AuthRequest authRequest) {
        // Authenticate using Spring Security's AuthenticationManager
        Authentication authenticate = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authRequest.getEmail(), authRequest.getPassword())
        );

        if (authenticate.isAuthenticated()) {
            // Generate token using email after successful authentication
            String token = service.generateToken(authRequest.getEmail());
            return ResponseEntity.ok(token);
        } else {
            throw new RuntimeException("Invalid access: Authentication failed");
        }
    }

    /**
     * Utility endpoint to validate if a token is still active.
     * Primarily used by the API Gateway to verify incoming requests.
     */
   
    @GetMapping("/validate")
    public ResponseEntity<String> validateToken(@RequestParam("token") String token) {
        // This calls the method we just added to AuthService
        service.validateToken(token);
        return ResponseEntity.ok("Token is valid");
    }
}