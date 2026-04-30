package com.cart.controller;

import com.cart.entity.CartItem;
import com.cart.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService service;

    /**
     * Adds an item to the cart. 
     * The 'username' is pulled from the Gateway Header for security.
     */
    @PostMapping("/add")
    public CartItem add(
            @RequestBody CartItem item, 
            @RequestHeader("loggedInUser") String username) {
        
        // We pass the authenticated username to the service layer
        return service.addToCart(item, username);
    }

    /**
     * Fetches the cart for the currently logged-in user.
     * Path variable removed to prevent unauthorized data access.
     */
    @GetMapping("/my-cart")
    public List<CartItem> getCart(
        @RequestHeader(value = "loggedInUser", required = false) String username) {
        
        System.out.println("Username received in Cart-Service: " + username);
        return service.getCartByUser(username);
    }
}