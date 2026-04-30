package com.auth.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Service class for handling JSON Web Token (JWT) operations.
 * This class provides methods to generate, create, and validate tokens
 * specifically updated for JJWT version 0.12.x.
 */
@Component
public class JwtService {

    /**
     * A 256-bit Base64 encoded secret key. 
     * In a production environment, this should be moved to an environment variable or a secure vault.
     */
    public static final String SECRET = "5367566B59703373367639792F423F4528482B4D6251655468576D5A71347437";

    /**
     * Validates the integrity and expiration of a given JWT token.
     * Uses the updated parser builder with {@code verifyWith} for modern JJWT security.
     * * @param token The JWT string to be validated.
     * @throws io.jsonwebtoken.JwtException if the token is invalid or expired.
     */
    public void validateToken(final String token) {
        Jwts.parser()
            .verifyWith(getSecretKey()) // Replaces deprecated setSigningKey()
            .build()
            .parseSignedClaims(token);  // Replaces deprecated parseClaimsJws()
    }

    /**
     * Generates a new JWT token for a specific user and assigns a role.
     * This is the entry point for creating a token after successful authentication.
     * * @param userName The identity of the user (usually username or email).
     * @param role The authorization level (e.g., "ADMIN" or "USER").
     * @return A signed JWT string containing the user's identity and role.
     */
    public String generateToken(String userName, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role); // Injected claim for Role-Based Access Control (RBAC)
        return createToken(claims, userName);
    }

    /**
     * Internal method to build the JWT string with specific claims and configurations.
     * Uses the updated Fluent API syntax (claims() and subject() instead of setClaims/setSubject).
     * * @param claims Custom attributes to include in the token payload.
     * @param userName The subject of the token.
     * @return A compact, URL-safe JWT string.
     */
    private String createToken(Map<String, Object> claims, String userName) {
        return Jwts.builder()
                .claims(claims)           // Sets custom claims (e.g., roles)
                .subject(userName)        // Sets the 'sub' (subject) claim
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 30)) // 30 minutes validity
                .signWith(getSecretKey()) // Automatically detects algorithm based on key size (HS256)
                .compact();
    }

    /**
     * Converts the Base64 encoded SECRET string into a cryptographic SecretKey.
     * Ensures that the key is compatible with HMAC-SHA algorithms.
     * * @return A {@link SecretKey} used for signing and verifying tokens.
     */
    private SecretKey getSecretKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}