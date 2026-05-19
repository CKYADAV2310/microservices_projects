package com.cart.service;

import java.util.List;
import com.cart.entity.CartItem;

public interface CartService {
    CartItem addToCart(CartItem item, String username);
    void updateQuantity(Long itemId, int quantity, String username);
    List<CartItem> getCartByUser(String username);
    void removeFromCart(Long itemId, String username);
    void clearCart(String targetUser);
}