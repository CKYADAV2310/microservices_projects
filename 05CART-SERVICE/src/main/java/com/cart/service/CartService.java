package com.cart.service;

import com.cart.client.ProductClient;
import com.cart.entity.CartItem;
import com.cart.repo.CartRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Service Layer for Cart Operations.
 * Integrates Feign Client for cross-service validation and Redis for performance.
 */
@Service
public class CartService {

    @Autowired
    private CartRepository repository;

    @Autowired
    private ProductClient productClient;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String CART_CACHE_PREFIX = "USER_CART_";

    /**
     * Adds an item to the cart. 
     * Pattern: "Validation -> Check -> Update/Save -> Cache Eviction"
     */
    public CartItem addToCart(CartItem item, String authenticatedUser) {
        
        // 1. Cross-service validation via Feign Client
        Object product = productClient.getProductById(item.getProductId());
        
        if (product != null) {
            // 2. Check for existing entry to prevent database duplicates
            CartItem savedItem = repository.findByUsernameAndProductId(authenticatedUser, item.getProductId())
                .map(existingItem -> {
                    existingItem.setQuantity(existingItem.getQuantity() + item.getQuantity());
                    return repository.save(existingItem);
                })
                .orElseGet(() -> {
                    item.setUsername(authenticatedUser); 
                    return repository.save(item);
                });

            // 3. EVICT CACHE: When cart changes, delete the old cached list so next GET is fresh
            redisTemplate.delete(CART_CACHE_PREFIX + authenticatedUser);
            
            return savedItem;
        } else {
            throw new RuntimeException("Validation Failed: Product not found in Catalog.");
        }
    }

    /**
     * Fetches cart items with Cache-Aside Pattern.
     * Logic: Check Redis first; if missing, fetch from MySQL and store in Redis.
     */
    public List<CartItem> getCartByUser(String username) {
        String cacheKey = CART_CACHE_PREFIX + username;

        // 1. Try fetching from Redis
        List<CartItem> cachedCart = (List<CartItem>) redisTemplate.opsForValue().get(cacheKey);
        if (cachedCart != null) {
            System.out.println("Cart fetched from Redis Cache for: " + username);
            return cachedCart;
        }

        // 2. Fetch from MySQL if not in Cache
        List<CartItem> cartList = repository.findByUsername(username);

        // 3. Store in Redis for 5 minutes
        redisTemplate.opsForValue().set(cacheKey, cartList, 5, TimeUnit.MINUTES);
        
        return cartList;
    }
    
    /**
     * Removes an item and clears the cache.
     */
    public void removeFromCart(Long id, String username) {
        repository.deleteById(id);
        // Clear cache so the user doesn't see the deleted item
        redisTemplate.delete(CART_CACHE_PREFIX + username);
    }
}