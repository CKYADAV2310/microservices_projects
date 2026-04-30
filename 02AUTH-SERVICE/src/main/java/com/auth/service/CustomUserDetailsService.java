package com.auth.service;

import com.auth.config.CustomUserDetails;
import com.auth.entity.UserCredential;
import com.auth.repo.UserCredentialRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Service used by Spring Security to load user data during login.
 * It is configured to use Email as the unique identifier.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserCredentialRepository repository;

    /**
     * This method is called by Spring Security's AuthenticationManager.
     * We have updated it to find the user by Email.
     * * @param email The email entered by the user in the login request.
     * @return UserDetails object required by Spring Security.
     * @throws UsernameNotFoundException if the email does not exist in the database.
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Look for the user in the database using the unique email
        Optional<UserCredential> credential = repository.findByEmail(email);

        // Convert our UserCredential entity into Spring Security's UserDetails format
        return credential.map(CustomUserDetails::new)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }
}