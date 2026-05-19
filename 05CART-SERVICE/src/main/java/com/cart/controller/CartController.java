package com.cart.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cart.dto.ApiResponse;
import com.cart.entity.CartItem;
import com.cart.service.CartServiceImpl;

import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartServiceImpl service;

    @Operation(summary = "Add item to cart")
    @PostMapping("/add")
    public ResponseEntity<CartItem> addToCart(
            @RequestBody CartItem item,
            @RequestHeader("loggedInUser") String username) {
        return ResponseEntity.ok(service.addToCart(item, username));
    }
    
    @Operation(summary = "Update item quantity directly from cart layout")
    @PutMapping("/update/{itemId}/{quantity}")
    public ResponseEntity<ApiResponse<Void>> updateQuantity(
            @PathVariable Long itemId,
            @PathVariable int quantity,
            @RequestHeader("loggedInUser") String username) {
        
        service.updateQuantity(itemId, quantity, username);
        return ResponseEntity.ok(new ApiResponse<>(true, "Quantity updated successfully", null));
    }

    @Operation(summary = "Get user's cart")
    @GetMapping("/my-cart")
    public ResponseEntity<List<CartItem>> getMyCart(@RequestHeader("loggedInUser") String username) {
        return ResponseEntity.ok(service.getCartByUser(username));
    }

    /**
     * Admin only access to clear a specific user's cart.
     */
    
    @DeleteMapping("/admin/clear/{targetUser}")
    public ResponseEntity<ApiResponse<String>> clearUserCart(
            @PathVariable String targetUser,
            @RequestHeader("role") String role) {
        
        // Check if the user has admin role
        if (role == null || !role.equalsIgnoreCase("ROLE_ADMIN")) {
            throw new AccessDeniedException("Access Denied: Admin privileges required.");
        }
        
        service.clearCart(targetUser);
        return ResponseEntity.ok(new ApiResponse<>(true, "Cart cleared successfully for: " + targetUser, null));
    }
    
    @Operation(summary = "Remove single item from cart")
    @DeleteMapping("/remove/{itemId}")
    public ResponseEntity<ApiResponse<Void>> removeFromCart(
            @PathVariable Long itemId,
            @RequestHeader("loggedInUser") String username) {
        
        service.removeFromCart(itemId, username);
        return ResponseEntity.ok(new ApiResponse<>(true, "Item removed from cart", null));
    }
}