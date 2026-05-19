package com.cart.config; 

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
	    return http
	        .csrf(csrf -> csrf.disable())
	        .authorizeHttpRequests(auth -> auth
	            // Explicitly allow Swagger UI, API Docs, and Webjars
	            .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/webjars/**").permitAll()
	            
	            // Allow functional paths (Gateway handles the actual token validation)
	            .requestMatchers("/cart/add", "/cart/my-cart", "/cart/admin/**").permitAll() 
	            
	            // Permit all for other routes to delegate security to the Gateway
	            .anyRequest().permitAll() 
	        )
	        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
	        .build();
	}

	/**
	 * Adds the "Authorize" button to the Cart Service Swagger UI.
	 */
	@Bean
	public OpenAPI customOpenAPI() {
	    return new OpenAPI()
	        .info(new Info()
	            .title("Cart Service API")
	            .version("1.0")
	            .description("Documentation for the Cart Microservice"))
	        .addSecurityItem(new SecurityRequirement().addList("BearerAuth"))
	        .components(new Components()
	            .addSecuritySchemes("BearerAuth", new SecurityScheme()
	                .name("BearerAuth")
	                .type(SecurityScheme.Type.HTTP)
	                .scheme("bearer")
	                .bearerFormat("JWT")));
	}
}