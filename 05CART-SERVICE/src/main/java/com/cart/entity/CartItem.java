package com.cart.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String username; // Taken from JWT via Gateway
    private Long productId;
    private String productName;
    private Double price;
    private Integer quantity;
}