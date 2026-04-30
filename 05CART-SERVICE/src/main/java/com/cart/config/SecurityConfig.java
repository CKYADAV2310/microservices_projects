package com.cart.config; //

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
	            // Allow Swagger and documentation
	            .requestMatchers("/cart/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
	            // Allow the Gateway to manage Admin security
	            .requestMatchers("/cart/add", "/cart/my-cart", "/cart/admin/**").permitAll() 
	            .anyRequest().permitAll() 
	        )
	        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
	        .build();
	}
}