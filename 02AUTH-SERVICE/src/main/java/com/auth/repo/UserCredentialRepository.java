package com.auth.repo;

import com.auth.entity.UserCredential;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserCredentialRepository extends JpaRepository<UserCredential, Integer> {
    Optional<UserCredential> findByEmail(String email);
    Optional<UserCredential> findByResetToken(String token); // To find user by OTP
}