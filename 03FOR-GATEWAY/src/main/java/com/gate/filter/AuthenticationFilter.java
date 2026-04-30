package com.gate.filter;

import com.gate.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * Global Gateway Filter for Authentication and Authorization (RBAC).
 * Intercepts incoming requests, validates tokens, and checks user roles.
 */
@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    @Autowired
    private RouteValidator validator;

    @Autowired
    private JwtUtil jwtUtil;

    public AuthenticationFilter() {
        super(Config.class);
    }

    /**
     * Core filter logic that executes for every request routed through the gateway.
     */
    @Override
    public GatewayFilter apply(Config config) {
        return ((exchange, chain) -> {
            
            // Step 1: Check if the route is secured (not in public list)
            if (validator.isSecured.test(exchange.getRequest())) {
                
                // Step 2: Validate the presence of the Authorization header
                if (!exchange.getRequest().getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                    throw new RuntimeException("Missing Authorization Header");
                }

                String authHeader = exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    authHeader = authHeader.substring(7);
                }

                try {
                    // Step 3: Validate Token Signature
                    jwtUtil.validateToken(authHeader);

                    // Step 4: Extract Role and Path for Authorization check
                    String role = jwtUtil.extractRole(authHeader);
                    String username = jwtUtil.extractUsername(authHeader);
                    String path = exchange.getRequest().getURI().getPath();

                    /**
                     * ROLE-BASED AUTHORIZATION:
                     * If the path is an admin path and user is not ADMIN, return 403 Forbidden.
                     */
                    if (path.contains("/admin") && !"ADMIN".equals(role)) {
                        exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                        return exchange.getResponse().setComplete();
                    }

                    /**
                     * HEADER PROPAGATION:
                     * Inject the username and role into headers for downstream microservices.
                     */
                    return chain.filter(exchange.mutate()
                            .request(exchange.getRequest().mutate()
                                    .header("loggedInUser", username)
                                    .header("role", role)
                                    .build())
                            .build());

                } catch (Exception e) {
                    // If token is invalid or expired
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return exchange.getResponse().setComplete();
                }
            }
            return chain.filter(exchange);
        });
    }

    public static class Config {
        // Configuration properties can be added here if needed
    }
}
