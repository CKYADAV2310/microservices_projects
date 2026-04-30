package com.cart.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Custom Exception for missing products in the Catalog.
 * The @ResponseStatus ensures that if the GlobalExceptionHandler is missing, 
 * Spring will still default to a 404 status.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ProductNotFoundException extends RuntimeException {

    public ProductNotFoundException(String message) {
        super(message);
    }
}