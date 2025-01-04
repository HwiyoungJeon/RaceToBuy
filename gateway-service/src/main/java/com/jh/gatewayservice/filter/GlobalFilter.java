package com.jh.gatewayservice.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import java.util.Base64;
import javax.crypto.SecretKey;

@Component
@Slf4j
public class GlobalFilter extends AbstractGatewayFilterFactory<GlobalFilter.Config> {

    private static final String FILTER_NAME = "GlobalFilter";

    @Value("${jwt.secret.key}")
    private String secretKey;

    public GlobalFilter() {
        super(Config.class);
    }

    @Override
    public String name() {
        return FILTER_NAME;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getPath().value();

            log.debug("Processing request: {} {}", request.getMethod(), path);
            log.debug("Request headers: {}", request.getHeaders());

            if (isPublicPath(path)) {
                return chain.filter(exchange);
            }

            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return handleError(exchange, "No valid authorization header", HttpStatus.UNAUTHORIZED);
            }

            String token = authHeader.substring(7);
            try {
                Claims claims = validateToken(token);

                ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                    .header("X-Authorization-Id", claims.get("id").toString())
                    .header("X-Authorization-Role", claims.get("role").toString())
                    .header(HttpHeaders.AUTHORIZATION, authHeader)
                    .build();

                log.debug("Modified request headers: {}", modifiedRequest.getHeaders());

                return chain.filter(exchange.mutate().request(modifiedRequest).build());
            } catch (Exception e) {
                log.error("Token validation failed", e);
                return handleError(exchange, "Invalid token", HttpStatus.UNAUTHORIZED);
            }
        };
    }

    private boolean isPublicPath(String path) {
        boolean isPublic = path.startsWith("/auth/") ||
                          path.startsWith("/login") ||
                          path.startsWith("/signup") ||
                          path.startsWith("/actuator/");
        log.debug("Path {} is public: {}", path, isPublic);
        return isPublic;
    }

    private String extractToken(ServerHttpRequest request) {
        String authHeader = request.getHeaders().getFirst("Authorization");
        log.debug("Authorization header: {}", authHeader);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    private Claims validateToken(String token) {
        try {
            // Base64로 디코딩된 시크릿 키 사용
            byte[] keyBytes = Base64.getDecoder().decode(secretKey);
            SecretKey key = Keys.hmacShaKeyFor(keyBytes);

            return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
        } catch (Exception e) {
            log.error("Token validation error: {}", e.getMessage());
            log.debug("Secret key being used: {}", secretKey);
            log.debug("Token being validated: {}", token);
            throw e;
        }
    }

    private Mono<Void> handleError(ServerWebExchange exchange, String message, HttpStatus status) {
        log.debug("Handling error: {} with status: {}", message, status);
        exchange.getResponse().setStatusCode(status);
        return exchange.getResponse().setComplete();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Config {
        private String baseMessage;
        private boolean preLogger;
        private boolean postLogger;
    }
}