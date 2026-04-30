package com.product.controller;

import com.product.entity.Product;
import com.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * Controller for Product management.
 * Administrative paths (/admin/**) are secured at the Gateway level.
 */
@RestController
@RequestMapping("/product")
@Tag(name = "Product API", description = "Endpoints for managing products")
public class ProductController {

    @Autowired
    private ProductService service;

    /**
     * Admin endpoint to add a new product.
     * Path is set to /admin to trigger Gateway's Role-Based Access Control.
     */
    @Operation(summary = "Add a new product", description = "Admin only access for adding items")
    @PostMapping("/admin/add")
    public ResponseEntity<Product> addProduct(
            @RequestBody Product product,
            @RequestHeader(value = "loggedInUser", required = false) String adminEmail) {
        
        // Logs the admin action for auditing purposes
        System.out.println("Product addition initiated by: " + adminEmail);
        Product savedProduct = service.saveProduct(product);
        return ResponseEntity.ok(savedProduct);
    }

    /**
     * Public endpoint to fetch all products.
     */
    @Operation(summary = "Get all products", description = "Fetches a list of all available products")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list")
    @GetMapping("/all")
    public List<Product> findAllProducts() {
        return service.getProducts();
    }

    /**
     * Fetch a single product by ID.
     */
    @GetMapping("/{id}")
    public Product findProductById(@PathVariable Long id) {
        return service.getProductById(id);
    }

    /**
     * Admin endpoint to delete a product.
     * Path is set to /admin to ensure only users with the 'ADMIN' role can execute this.
     */
    @Operation(summary = "Delete a product", description = "Admin only access for removing items")
    @DeleteMapping("/admin/delete/{id}")
    public ResponseEntity<String> deleteProduct(
            @PathVariable Long id,
            @RequestHeader(value = "loggedInUser", required = false) String adminEmail) {
        
        System.out.println("Product deletion for ID " + id + " requested by: " + adminEmail);
        String response = service.deleteProduct(id);
        return ResponseEntity.ok(response);
    }
}