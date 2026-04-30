package com.cart.client;



import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "PRODUCT-SERVICE") 
public interface ProductClient {

    @GetMapping("/product/{id}")
    public Object getProductById(@PathVariable("id") Long id);
}