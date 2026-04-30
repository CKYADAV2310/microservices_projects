package com.cart.service;

import com.cart.client.ProductClient;
import com.cart.dto.ProductDTO; 
import com.cart.entity.CartItem;
import com.cart.exception.ProductNotFoundException; 
import com.cart.repo.CartRepository;
import lombok.extern.slf4j.Slf4j; // Proper Logging
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j // Lombok annotation for logging
public class CartService {

    @Autowired
    private CartRepository repository;

    @Autowired
    private ProductClient productClient;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String CART_CACHE_PREFIX = "USER_CART_";

    /**
     * @Transactional ensures that if database save fails, cache eviction doesn't happen inconsistently.
     */
    @Transactional
    public CartItem addToCart(CartItem item, String authenticatedUser) {
        log.info("Adding product {} to cart for user {}", item.getProductId(), authenticatedUser);

        // 1. Fetch Typed DTO (Validation)
        ProductDTO product = productClient.getProductById(item.getProductId());
        
        if (product == null) {
            throw new ProductNotFoundException("Product with ID " + item.getProductId() + " does not exist.");
        }

        // 2. Map enriched details from Product-Service to CartItem
        // This solves your 'null' fields problem permanently
        item.setProductName(product.getName());
        item.setPrice(product.getPrice());

        // 3. Upsert Logic (Update if exists, else Save)
        CartItem savedItem = repository.findByUsernameAndProductId(authenticatedUser, item.getProductId())
            .map(existingItem -> {
                existingItem.setQuantity(existingItem.getQuantity() + item.getQuantity());
                // Update price in case it changed in the Product-Service
                existingItem.setPrice(product.getPrice()); 
                return repository.save(existingItem);
            })
            .orElseGet(() -> {
                item.setUsername(authenticatedUser); 
                return repository.save(item);
            });

        // 4. Cache Eviction (Write-Through/Evict Pattern)
        evictCache(authenticatedUser);
        
        return savedItem;
    }

    /**
     * Cache-Aside Pattern with proper generics
     */
    @SuppressWarnings("unchecked")
    public List<CartItem> getCartByUser(String username) {
        String cacheKey = CART_CACHE_PREFIX + username;

        try {
            List<CartItem> cachedCart = (List<CartItem>) redisTemplate.opsForValue().get(cacheKey);
            if (cachedCart != null) {
                log.debug("Cache hit for user: {}", username);
                return cachedCart;
            }
        } catch (Exception e) {
            log.error("Redis error: {}", e.getMessage()); // Don't let Redis failure crash the app
        }

        List<CartItem> cartList = repository.findByUsername(username);
        
        // Populate cache asynchronously or with timeout
        redisTemplate.opsForValue().set(cacheKey, cartList, 15, TimeUnit.MINUTES);
        
        return cartList;
    }

    @Transactional
    public void removeFromCart(Long id, String username) {
        repository.deleteById(id);
        evictCache(username);
    }

    @Transactional
    public void clearCart(String username) {
        repository.deleteByUsername(username);
        evictCache(username);
        log.info("Cart cleared for user: {}", username);
    }

    private void evictCache(String username) {
        redisTemplate.delete(CART_CACHE_PREFIX + username);
    }
}