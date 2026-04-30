package com.gate.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;


@Configuration
@EnableWebFluxSecurity
public class GatewaySecurityConfig {

	@Bean
	public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
	    return http
	        .csrf(ServerHttpSecurity.CsrfSpec::disable)
	        .cors(ServerHttpSecurity.CorsSpec::disable) // Disable CORS for local dev
	        .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
	        .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
	        .authorizeExchange(exchange -> exchange
	            // Allow everything for now so we can debug the logic
	            .pathMatchers("/auth/**", "/v3/api-docs/**", "/swagger-ui/**", "/webjars/**").permitAll()
	            .anyExchange().permitAll() 
	        )
	        .build();
	}
}