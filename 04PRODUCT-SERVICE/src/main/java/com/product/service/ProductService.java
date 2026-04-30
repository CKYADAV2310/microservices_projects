package com.product.service;

import com.product.entity.Product;
import com.product.repo.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Service Layer for Product Management.
 * Implements the "Cache-Aside" pattern using Redis and 
 * "Event-Driven" communication using Apache Kafka.
 */
@Service
public class ProductService {

    @Autowired
    private ProductRepository repository;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    // Standard prefix for Redis keys to avoid collisions with other services
    private static final String CACHE_KEY_PREFIX = "PRODUCT_";

    /**
     * Saves a product to MySQL and updates the Redis cache.
     * Putting data in cache during save prevents a "Cache Miss" on the first read.
     */
    public Product saveProduct(Product product) {
        Product savedProduct = repository.save(product);
        
        // Update Cache: Key = PRODUCT_ID, Value = Product Object
        // Expiry set to 10 minutes to keep memory clean
        redisTemplate.opsForValue().set(CACHE_KEY_PREFIX + savedProduct.getId(), 
                                        savedProduct, 10, TimeUnit.MINUTES);
        return savedProduct;
    }

    /**
     * Fetches all products directly from the database.
     * Caching full lists is usually avoided unless the list is small and static.
     */
    public List<Product> getProducts() {
        return repository.findAll();
    }

    /**
     * Implements the Cache-Aside Pattern:
     * 1. Check Redis for the data.
     * 2. If present (Cache Hit), return it immediately.
     * 3. If absent (Cache Miss), fetch from MySQL, save to Redis, and return.
     */
    public Product getProductById(Long id) {
        String key = CACHE_KEY_PREFIX + id;

        // Step 1: Query Redis (RAM-based, O(1) complexity)
        Product cachedProduct = (Product) redisTemplate.opsForValue().get(key);
        if (cachedProduct != null) {
            return cachedProduct; 
        }

        // Step 2: Query MySQL (Disk-based, slower)
        Product product = repository.findById(id).orElse(null);

        // Step 3: Populate Cache for the next request
        if (product != null) {
            redisTemplate.opsForValue().set(key, product, 10, TimeUnit.MINUTES);
        }
        return product;
    }

    /**
     * Deletes product from DB and cleans up external systems.
     * 1. Remove from MySQL.
     * 2. Evict (delete) from Redis to prevent stale data.
     * 3. Notify Cart-Service via Kafka to remove item from users' carts.
     */
    public String deleteProduct(Long id) {
        repository.deleteById(id);

        // Cache Eviction: Ensures no one reads a product that no longer exists
        redisTemplate.delete(CACHE_KEY_PREFIX + id);

        // Kafka Event: Asynchronous notification to other microservices
        kafkaTemplate.send("product-events", "DELETED:" + id);

        return "Product removed !! " + id;
    }
}