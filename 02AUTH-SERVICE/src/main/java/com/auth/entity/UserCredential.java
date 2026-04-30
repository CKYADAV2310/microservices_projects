package com.auth.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity representing user credentials. 
 * Enforces unique email for login while allowing duplicate display names.
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
    private String username; // Now used as a Display Name (can be duplicate)
    
    @Column(unique = true, nullable = false)
    private String email; // Primary unique identifier for Login
    
    @Column(nullable = false)
    private String password;
    
    private String role; // ADMIN or USER
}