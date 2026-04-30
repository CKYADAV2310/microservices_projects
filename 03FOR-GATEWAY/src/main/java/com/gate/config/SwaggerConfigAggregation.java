package com.gate.config;

import org.springdoc.core.properties.AbstractSwaggerUiConfigProperties.SwaggerUrl;
import org.springdoc.core.properties.SwaggerUiConfigProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import jakarta.annotation.PostConstruct;
import java.util.Set;
import java.util.HashSet;

@Configuration
@Primary
public class SwaggerConfigAggregation {

    private final SwaggerUiConfigProperties swaggerUiConfigProperties;

    public SwaggerConfigAggregation(SwaggerUiConfigProperties swaggerUiConfigProperties) {
        this.swaggerUiConfigProperties = swaggerUiConfigProperties;
    }

    @PostConstruct
    public void init() {
        Set<SwaggerUrl> urls = new HashSet<>();
        // Add all your services here
        urls.add(new SwaggerUrl("Auth-Service", "/auth/v3/api-docs", "Auth-Service"));
        urls.add(new SwaggerUrl("Product-Service", "/product/v3/api-docs", "Product-Service"));
        urls.add(new SwaggerUrl("Cart-Service", "/cart/v3/api-docs", "Cart-Service"));
        
        swaggerUiConfigProperties.setUrls(urls);
    }
}
