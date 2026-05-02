package com.gate.filter;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import java.util.*;
import java.util.function.Predicate;

@Component
public class RouteValidator {

	// List of endpoints that do not require authentication
	public static final List<String> openApiEndpoints = List.of(
	        "/auth/register", 
	        "/auth/login", 
	        "/auth/v3/api-docs",   
	        "/product/v3/api-docs", 
	        "/v3/api-docs", 
	        "/swagger-ui", 
	        "/webjars", 
	        "/auth/forgot-password" 	,  
            "/auth/reset-password"   
	);

    public Predicate<ServerHttpRequest> isSecured =
            request -> openApiEndpoints
                    .stream()
                    .noneMatch(uri -> request.getURI().getPath().contains(uri));
}