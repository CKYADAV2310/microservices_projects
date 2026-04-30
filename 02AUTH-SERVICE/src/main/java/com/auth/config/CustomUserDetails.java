package com.auth.config;

import com.auth.entity.UserCredential;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * A wrapper class that converts our UserCredential entity 
 * into a format Spring Security understands.
 */
public class CustomUserDetails implements UserDetails {

    private String email;    // Using Email as the primary identity
    private String password;
    private String role;     // Added to handle user permissions

    public CustomUserDetails(UserCredential userCredential) {
        // We set the 'email' to the 'username' field of UserDetails
        this.email = userCredential.getEmail();
        this.password = userCredential.getPassword();
        this.role = userCredential.getRole();
    }

    /**
     * Converts the user's role string into a GrantedAuthority object.
     * This is used by Spring Security for role-based access.
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role));
    }

    @Override
    public String getPassword() {
        return password;
    }

    /**
     * Returns the unique identifier for the user.
     * In our system, this is the Email.
     */
    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}