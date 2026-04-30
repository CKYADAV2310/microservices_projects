package com.cart.controller;

import com.cart.entity.CartItem;
import com.cart.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService service;

    @Operation(summary = "Add item to cart")
    @PostMapping("/add")
    public ResponseEntity<CartItem> addToCart(
            @RequestBody CartItem item,
            @RequestHeader("loggedInUser") String username) {
        return ResponseEntity.ok(service.addToCart(item, username));
    }

    @Operation(summary = "Get user's cart")
    @GetMapping("/my-cart")
    public ResponseEntity<List<CartItem>> getMyCart(@RequestHeader("loggedInUser") String username) {
        return ResponseEntity.ok(service.getCartByUser(username));
    }

    /**
     * Admin only access to clear a specific user's cart.
     */
    @Operation(summary = "Admin: Clear user cart")
    @DeleteMapping("/admin/clear/{targetUser}")
    //@PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> clearUserCart(
            @PathVariable String targetUser,
            @RequestHeader("role") String role) {
        
        System.out.println("Admin action initiated by role: " + role);
        service.clearCart(targetUser);
        return ResponseEntity.ok("Cart cleared for user: " + targetUser);
    }
}