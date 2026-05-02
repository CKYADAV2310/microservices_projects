package com.auth.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * Entity representing user credentials. 
 * Enforces unique email for login while allowing duplicate display names.
 * Includes fields for Password Reset functionality.
 */
@Entity
@Table(name = "user_credentials")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserCredential {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    
    @Column(nullable = false)
    private String username; // Display name, can be duplicated
    
    @Column(unique = true, nullable = false)
    private String email; // Unique email for login
    
    @Column(nullable = false)
    private String password;
    
    private String role; // User role (e.g., USER, ADMIN)

    // Fields for Password Reset functionality
    @Column(name = "reset_token")
    private String resetToken;
    
// Token expiry time for password reset, can be null if no reset is in progress
    @Column(name = "token_expiry")
    private LocalDateTime tokenExpiry;
}