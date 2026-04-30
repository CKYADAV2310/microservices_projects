package com.gate.filter;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import java.util.*;
import java.util.function.Predicate;

@Component
public class RouteValidator {

	// Inside RouteValidator.java
	public static final List<String> openApiEndpoints = List.of(
	        "/auth/register",
	        "/auth/token",
	        "/auth/v3/api-docs",    // Added this
	        "/product/v3/api-docs", // Added this
	        "/v3/api-docs",
	        "/swagger-ui",
	        "/webjars"
	);

    public Predicate<ServerHttpRequest> isSecured =
            request -> openApiEndpoints
                    .stream()
                    .noneMatch(uri -> request.getURI().getPath().contains(uri));
}