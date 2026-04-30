package com.product.service;

import com.product.entity.Product;
import com.product.repo.ProductRepository;
import lombok.extern.slf4j.Slf4j; // Norm: Use logging
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Norm: Ensure Atomicity

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
    public String deleteProduct(Long id) {
        if (!repository.existsById(id)) {
            return "Product not found with ID: " + id;
        }

        repository.deleteById(id);

        // Evict Cache
        redisTemplate.delete(CACHE_KEY_PREFIX + id);

        // MATCHING THE CONSUMER: Send the raw Long ID to "product-deletion-topic"
        kafkaTemplate.send("product-deletion-topic", id);
        
        log.info("Product {} deleted and sync event broadcasted", id);
        return "Product removed !! " + id;
    }
}