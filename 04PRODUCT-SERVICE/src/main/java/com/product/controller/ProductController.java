package com.product.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.product.entity.Product;
import com.product.service.ProductService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j; // Use SLF4J for logging

@RestController
@RequestMapping("/product")
@Tag(name = "Product API", description = "Endpoints for managing products")
@Slf4j 
public class ProductController {

    @Autowired
    private ProductService service;

    /**
     * Admin endpoint to add a new product.
     */
    @Operation(summary = "Add a new product", description = "Admin only access for adding items")
    @PostMapping("/admin/add")
    public ResponseEntity<Product> addProduct(
            @RequestBody Product product,
            @RequestHeader(value = "loggedInUser", required = false) String adminEmail) {
        
        log.info("Product addition initiated by: {}", adminEmail);
        Product savedProduct = service.saveProduct(product);
        // Using 201 Created is standard for POST operations
        return new ResponseEntity<>(savedProduct, HttpStatus.CREATED);
    }

    /**
     * Public endpoint to fetch all products.
     */
    @Operation(summary = "Get all products", description = "Fetches a list of all available products")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list")
    @GetMapping("/all")
    public ResponseEntity<List<Product>> findAllProducts() {
        return ResponseEntity.ok(service.getProducts());
    }

    /**
     * Fetch single product. 
     * This is the endpoint called by Cart-Service via Feign.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Product> findProductById(@PathVariable Long id) {
        Product product = service.getProductById(id);
        if (product == null) {
            // This triggers the 404 logic in the Feign Client
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(product);
    }

    /**
     * Admin endpoint to delete a product.
     */
    @Operation(summary = "Delete a product", description = "Admin only access for removing items")
    @DeleteMapping("/admin/delete/{id}")
    public ResponseEntity<String> deleteProduct(
            @PathVariable Long id,
            @RequestHeader(value = "loggedInUser", required = false) String adminEmail,
            @RequestHeader(value = "role", required = false) String role) {
        
        // 1. Check if the user is an admin
        if (role == null || !role.equalsIgnoreCase("ROLE_ADMIN")) {
            throw new AccessDeniedException("Only admins can delete products");
        }

        log.info("Product deletion for ID {} requested by admin: {}", id, adminEmail);
        
        // 2. Call the service
        service.deleteProduct(id);
        
        // 3. Return 200 OK with a success message
        return ResponseEntity.ok("Product with ID " + id + " has been successfully deleted. Kafka cleanup event triggered.");
    }
}