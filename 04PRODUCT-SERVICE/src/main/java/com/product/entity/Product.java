package com.product.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity(name = "products")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Product {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
 
    private String description;
    
    private double price;
    
    private int quantity;
    
    @Column(name = "category")
    private String category; 
    
    @Column(length = 500)  // Assuming the image URL can be long, we set a length for the column
    private String imageUrl; 
}