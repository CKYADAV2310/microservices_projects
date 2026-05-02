package com.cart.repo;

import com.cart.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

public interface CartRepository extends JpaRepository<CartItem, Long> {

    List<CartItem> findByUsername(String username);
    Optional<CartItem> findByUsernameAndProductId(String username, Long productId);

    /**
     * FIX: For custom delete methods, you MUST add @Modifying and @Transactional
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM CartItem c WHERE c.username = :username")
    void deleteByUsername(String username);

    /**
     * This will be used by your Kafka Consumer to clean up deleted products
     */
    @Modifying
    @Transactional
    void deleteByProductId(Long productId);
}