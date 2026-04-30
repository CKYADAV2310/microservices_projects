package com.gate.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;

/**
 * Utility class for JWT (JSON Web Token) parsing and validation.
 * Updated to use JJWT 0.12.x modern syntax (verifyWith, getPayload).
 */
@Component
public class JwtUtil {

    /**
     * 256-bit Secret Key for HMAC-SHA signing. 
     * Shared with Auth-Service to ensure token integrity.
     */
    public static final String SECRET = "5367566B59703373367639792F423F4528482B4D6251655468576D5A71347437";

    /**
     * Validates the token's signature and expiration status.
     * @param token The JWT string to be validated.
     */
    public void validateToken(final String token) {
        Jwts.parser()
            .verifyWith(getSecretKey())
            .build()
            .parseSignedClaims(token);
    }

    /**
     * Extracts the 'role' claim from the token payload.
     * This role is used for Role-Based Access Control (RBAC) at the Gateway.
     * @param token The JWT string.
     * @return String representing the user's role (e.g., ADMIN, USER).
     */
    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

    /**
     * Extracts the 'subject' (username) from the token.
     * @param token The JWT string.
     * @return The username embedded in the token.
     */
    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    /**
     * Parses the JWT and returns the payload (Claims).
     * Uses the modern JJWT .getPayload() method instead of deprecated .getBody().
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Decodes the Base64 SECRET and generates a cryptographic SecretKey.
     */
    private SecretKey getSecretKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}