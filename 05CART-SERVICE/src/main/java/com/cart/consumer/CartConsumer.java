package com.cart.consumer;

import com.cart.repo.CartRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CartConsumer {

    @Autowired
    private CartRepository repository;

    /**
     * Listens for Product IDs deleted from the Product-Service.
     * Triggers a bulk delete across all user carts in the database.
     */
    @KafkaListener(topics = "product-deletion-topic", groupId = "cart-group")
    public void consumeProductDeletion(Long productId) {
        log.info("Kafka Event Received: Product {} has been deleted. Syncing carts...", productId);
        
        try {
            // This calls the @Modifying @Query method in your CartRepository
            repository.deleteByProductId(productId);
            log.info("Successfully removed Product {} from all active carts.", productId);
        } catch (Exception e) {
            log.error("Failed to sync cart for deleted product {}: {}", productId, e.getMessage());
        }
    }
}