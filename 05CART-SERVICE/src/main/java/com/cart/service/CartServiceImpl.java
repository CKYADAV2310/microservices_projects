package com.cart.service;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cart.client.ProductClient;
import com.cart.dto.ProductDTO;
import com.cart.entity.CartItem;
import com.cart.exception.ProductNotFoundException;
import com.cart.repo.CartRepository;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j 
public class CartServiceImpl implements CartService {

    @Autowired
    private CartRepository repository;

    @Autowired
    private ProductClient productClient;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String CART_CACHE_PREFIX = "USER_CART_";

    @Transactional
    public CartItem addToCart(CartItem item, String authenticatedUser) {
        log.info("Adding product {} to cart for user {}", item.getProductId(), authenticatedUser);

        ProductDTO product = productClient.getProductById(item.getProductId());
        if (product == null) {
            throw new ProductNotFoundException("Product with ID " + item.getProductId() + " does not exist.");
        }

        //  FIX: Map the unique dynamic Image link from ProductDTO down to your CartItem Entity row!
        item.setProductName(product.getName());
        item.setPrice(product.getPrice());
        item.setImageUrl(product.getImageUrl()); // Make sure this setter exists on your CartItem entity

        CartItem savedItem = repository.findByUsernameAndProductId(authenticatedUser, item.getProductId())
            .map(existingItem -> {
                existingItem.setQuantity(existingItem.getQuantity() + item.getQuantity());
                existingItem.setPrice(product.getPrice()); 
                existingItem.setImageUrl(product.getImageUrl()); // Keep data synced on price updates
                return repository.save(existingItem);
            })
            .orElseGet(() -> {
                item.setUsername(authenticatedUser); 
                return repository.save(item);
            });

        evictCache(authenticatedUser);
        return savedItem;
    }

    @Transactional //  Ensure updates run inside a transaction
    public void updateQuantity(Long itemId, int quantity, String username) {
        CartItem cartItem = repository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with id: " + itemId));
                
        if (!cartItem.getUsername().equalsIgnoreCase(username)) {
            throw new AccessDeniedException("Unauthorized action on this cart item.");
        }
        
        cartItem.setQuantity(quantity);
        repository.save(cartItem);

        // CRITICAL CACHE CORRECTION: Evict Redis data when changing counts, 
        // otherwise front-end will display stale pricing on page re-renders!
        evictCache(username); 
    }

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
            log.error("Redis error: {}", e.getMessage()); 
        }

        List<CartItem> cartList = repository.findByUsername(username);
        
        try {
            redisTemplate.opsForValue().set(cacheKey, cartList, 15, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.error("Failed to write to Redis cache: {}", e.getMessage());
        }
        
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
        try {
            redisTemplate.delete(CART_CACHE_PREFIX + username);
        } catch (Exception e) {
            log.error("Redis eviction failed: {}", e.getMessage());
        }
    }
}