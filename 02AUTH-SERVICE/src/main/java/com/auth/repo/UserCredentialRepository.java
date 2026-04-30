package com.auth.repo;

import com.auth.entity.UserCredential;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * Repository interface for database operations on UserCredential.
 */
public interface UserCredentialRepository extends JpaRepository<UserCredential, Integer> {
    /**
     * Finds a user by their unique email address.
     * @param email The login email.
     * @return Optional user credential.
     */
    Optional<UserCredential> findByEmail(String email);
}