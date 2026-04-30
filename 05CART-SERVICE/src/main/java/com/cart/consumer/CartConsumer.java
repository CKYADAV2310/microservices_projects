package com.cart.consumer;

import com.cart.repo.CartRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

/**
 * Listens for messages from Apache Kafka.
 * Documentation: Ensures that the Cart data remains consistent with the Product data.
 */
@Service
public class CartConsumer {

    @Autowired
    private CartRepository cartRepository;

    /**
     * Subscribes to 'product-events' topic.
     * Format expected: "DELETED:101"
     */
    @KafkaListener(topics = "product-events", groupId = "cart-group")
    public void handleProductDeletion(String message) {
        System.out.println("Message received from Kafka: " + message);

        if (message.startsWith("DELETED:")) {
            try {
                Long productId = Long.parseLong(message.split(":")[1]);
                // Perform global cleanup in the Cart database
                cartRepository.deleteByProductId(productId);
                System.out.println("Global cleanup complete for Product ID: " + productId);
            } catch (Exception e) {
                System.err.println("Error processing Kafka message: " + e.getMessage());
            }
        }
    }
}