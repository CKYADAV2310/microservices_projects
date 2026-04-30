package com.product.config; 

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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
	            // Public paths
	            .requestMatchers(HttpMethod.GET, "/product/{id}").permitAll()
	            .requestMatchers("/product/all", "/product/v3/api-docs/**", "/swagger-ui/**").permitAll()
	            
	            // Allow the Gateway to manage Admin security
	            // If the Gateway already validated the ROLE_ADMIN, let it pass through here
	            .requestMatchers("/product/admin/**").permitAll() 
	            
	            // This is likely what is triggering the 401 for other internal calls
	            .anyRequest().permitAll() 
	        )
	        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
	        .build();
	}
}