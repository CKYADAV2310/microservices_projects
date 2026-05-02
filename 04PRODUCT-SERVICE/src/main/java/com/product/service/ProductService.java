package com.product.service;

import com.product.entity.Product;
import com.product.exception.ProductNotFoundException;
import com.product.repo.ProductRepository;
import lombok.extern.slf4j.Slf4j; 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; 
// We use @Transactional to ensure that if any part of the delete operation fails (DB, Redis, Kafka), we can roll back the entire transaction to maintain data integrity.

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class ProductService {

    @Autowired
    private ProductRepository repository;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // Changed to Long to match the Consumer's expected value-deserializer
    @Autowired
    private KafkaTemplate<String, Long> kafkaTemplate;

    private static final String CACHE_KEY_PREFIX = "PRODUCT_";

    @Transactional
    public Product saveProduct(Product product) {
        Product savedProduct = repository.save(product);
        try {
            redisTemplate.opsForValue().set(CACHE_KEY_PREFIX + savedProduct.getId(), 
                                            savedProduct, 10, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.error("Redis Cache update failed: {}", e.getMessage());
        }
        return savedProduct;
    }

    public List<Product> getProducts() {
        return repository.findAll();
    }

    public Product getProductById(Long id) {
        String key = CACHE_KEY_PREFIX + id;
        try {
            Product cachedProduct = (Product) redisTemplate.opsForValue().get(key);
            if (cachedProduct != null) {
                log.info("Cache Hit for Product ID: {}", id);
                return cachedProduct; 
            }
        } catch (Exception e) {
            log.error("Redis unreachable, falling back to DB: {}", e.getMessage());
        }

        Product product = repository.findById(id).orElse(null);

        if (product != null) {
            redisTemplate.opsForValue().set(key, product, 10, TimeUnit.MINUTES);
        }
        return product;
    }

    /**
     * TRANSACTIONAL: If Kafka or Redis fail, the DB delete can roll back 
     * or be handled as a single unit of work.
     */
    @Transactional
    public void deleteProduct(Long id) {
        Product product = repository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + id));

        repository.delete(product);

        // Evict Cache
        //
        redisTemplate.delete(CACHE_KEY_PREFIX + id);

        // Asynchronous Kafka Send
        // We send the ID of the deleted product to Kafka. The Cart-Service will listen to this topic and remove any cart items that reference this product.
        kafkaTemplate.send("product-deletion-topic", id).whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Message sent to Kafka successfully for ID: {}", id);
                // We can also log the partition and offset if needed
            } else {
                log.error("Failed to send Kafka message: {}", ex.getMessage());
                // Depending on requirements, we can  implement a retry mechanism here
            }
        });
    }
    
}