package com.auth.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.auth.entity.UserCredential;
import com.auth.repo.UserCredentialRepository;

/**
 * Service class handling the business logic for User Authentication.
 * Connects the Database Repository with the JWT Utility.
 */
@Service
public class AuthService {

    @Autowired
    private UserCredentialRepository repository;

    @Autowired
    private JwtService jwtService;

    /**
     * Injected bean to handle password encryption.
     * Ensure you have a PasswordEncoder @Bean defined in your AuthConfig.
     */
    @Autowired
    private PasswordEncoder passwordEncoder; 

    /**
     * Saves a new user to the database.
     * Encrypts the password before saving for security.
     */
    public String saveUser(UserCredential credential) {
        // Now passwordEncoder will be recognized
        credential.setPassword(passwordEncoder.encode(credential.getPassword()));
        repository.save(credential);
        return "User registered successfully!";
    }

    /**
     * Generates a JWT token after looking up the user by their unique email.
     * @param email The user's unique login email.
     * @return A signed JWT string containing the username and role.
     */
    public String generateToken(String email) {
        UserCredential user = repository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        
        // Passing username and role to the JWT generator
        return jwtService.generateToken(user.getUsername(), user.getRole());
    }
    
    

    /**
     * Validates the structure and signature of an existing JWT token.
     * This method is primarily used by the API Gateway to verify incoming requests.
     * @param token The JWT string to be validated.
     */
    /**
     * Checks if the token is valid.
     * The Gateway calls this through the Controller.
     */
    public void validateToken(String token) {
        // Calls the validation logic in JwtService
        jwtService.validateToken(token);
    }
}