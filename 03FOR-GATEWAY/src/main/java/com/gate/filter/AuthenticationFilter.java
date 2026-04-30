package com.gate.filter;

import com.gate.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    @Autowired
    private RouteValidator validator;

    @Autowired
    private JwtUtil jwtUtil;

    public AuthenticationFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return ((exchange, chain) -> {
            if (validator.isSecured.test(exchange.getRequest())) {
                // 1. Check if Header exists
                if (!exchange.getRequest().getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                    throw new RuntimeException("Missing Authorization Header");
                }

                String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
                
                // 2. Validate prefix format
                if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                    throw new RuntimeException("Invalid Authorization Header Format");
                }

                try {
                    // 3. STRIP PREFIX: Remove "Bearer " (7 characters) before passing to JwtUtil
                    String token = authHeader.substring(7);

                    // 4. Validate the actual token string
                    jwtUtil.validateToken(token);

                    String role = jwtUtil.extractRole(token); 
                    String username = jwtUtil.extractUsername(token);
                    String path = exchange.getRequest().getURI().getPath();

                    // Debug Logs - Check these in your STS console
                    System.out.println("Gateway: Validating Token for User: " + username);
                    System.out.println("Gateway: Path: " + path + " | Role: " + role);

                    // 5. Gateway-level Admin Check
                    if (path.contains("/admin") && !role.toUpperCase().contains("ADMIN")) {
                        exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                        return exchange.getResponse().setComplete();
                    }

                    // 6. Propagate prefixed role (Spring Security requires ROLE_ prefix)
                    String downstreamRole = role.startsWith("ROLE_") ? role : "ROLE_" + role;

                    return chain.filter(exchange.mutate()
                            .request(exchange.getRequest().mutate()
                                    .header("loggedInUser", username)
                                    .header("role", downstreamRole) 
                                    .build())
                            .build());
                            
                } catch (Exception e) {
                    // Log the actual error to see why it's failing (Expired? Signature mismatch?)
                    System.out.println("Gateway Auth Error: " + e.getMessage());
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return exchange.getResponse().setComplete();
                }
            }
            return chain.filter(exchange);
        });
    }

    public static class Config {}
}