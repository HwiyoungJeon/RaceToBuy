package com.jh.gatewayservice.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Base64;
import java.util.Optional;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR;

@Component
@Slf4j
public class GlobalFilter extends AbstractGatewayFilterFactory<GlobalFilter.Config> {

    @Value("${jwt.secret.key}")
    private String jwtSecret;

    public GlobalFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getURI().getPath();

            log.info("Processing request path: {}", path);
            log.info("Request URI: {}", request.getURI());
            log.info("Request method: {}", request.getMethod());
            log.info("Request headers: {}", request.getHeaders());
            log.info("Gateway route destination: {}",
                    Optional.ofNullable(exchange.getAttribute(GATEWAY_REQUEST_URL_ATTR)));
            log.info("JWT Secret Key: {}", jwtSecret);

            if (isPublicPath(path)) {
                log.info("Public path accessed: {}", path);
                return chain.filter(exchange);
            }

            String token = request.getHeaders().getFirst("Authorization");
            log.info("Authorization header value: '{}'", token);

            if (token == null) {
                log.error("Token is null");
                return onError(exchange, "Token is missing", HttpStatus.UNAUTHORIZED);
            }

            if (!token.startsWith("Bearer ")) {
                log.error("Token does not start with 'Bearer ': {}", token);
                return onError(exchange, "Invalid token format", HttpStatus.UNAUTHORIZED);
            }

            try {
                String actualToken = token.substring(7);
                log.info("Token after removing 'Bearer ': '{}'", actualToken);

                Claims claims = Jwts.parser()
                        .setSigningKey(Base64.getDecoder().decode(jwtSecret))
                        .parseClaimsJws(actualToken)
                        .getBody();

                log.info("Successfully parsed token. Claims: {}", claims);

                ServerHttpRequest modifiedRequest = request.mutate()
                        .header("X-Authorization-Id", String.valueOf(claims.get("id")))
                        .header("X-Authorization-Email", String.valueOf(claims.get("email")))
                        .header("X-Authorization-Role", String.valueOf(claims.get("role")))
                        .build();

                return chain.filter(exchange.mutate().request(modifiedRequest).build());
            } catch (Exception e) {
                log.error("Token validation failed: {}", e.getMessage(), e);
                return onError(exchange, "Invalid token: " + e.getMessage(), HttpStatus.UNAUTHORIZED);
            }
        };
    }

    private boolean isPublicPath(String path) {
        return path.startsWith("/auth/") ||
                path.startsWith("/login") ||
                path.startsWith("/signup");
    }

    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus status) {
        log.error("Error during request processing: {}", message);
        exchange.getResponse().setStatusCode(status);
        return exchange.getResponse().setComplete();
    }

    @Data
    public static class Config {
        private String baseMessage;
        private boolean preLogger;
        private boolean postLogger;
    }
}