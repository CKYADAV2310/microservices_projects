package com.cart.repo;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import com.cart.entity.CartItem;

/**
 * Repository for managing Cart persistence.
 * Includes custom queries for user-specific carts and global cleanup.
 */
public interface CartRepository extends JpaRepository<CartItem, Long> {
    
    // Fetch all items for a specific user
    List<CartItem> findByUsername(String username);

    // Check if a product already exists in a user's cart (to increment quantity instead of adding new row)
    Optional<CartItem> findByUsernameAndProductId(String username, Long productId);

    /**
     * CRITICAL: Triggered by Kafka Consumer.
     * Removes a specific product from EVERY user's cart if that product is deleted 
     * from the Product-Service.
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM CartItem c WHERE c.productId = :productId")
    void deleteByProductId(Long productId);
}