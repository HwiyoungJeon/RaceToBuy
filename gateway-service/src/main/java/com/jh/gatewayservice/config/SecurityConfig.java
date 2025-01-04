package com.jh.gatewayservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        return http
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            .cors(corsSpec -> corsSpec.configurationSource(corsConfigurationSource()))
            .authorizeExchange(exchanges -> exchanges
                .pathMatchers(
                    "/auth/**", 
                    "/login/**", 
                    "/signup/**", 
                    "/orders/**",          // 임시로 추가
                    "/products/**",        // 임시로 추가
                    "/users/**"           // 임시로 추가
                ).permitAll()
                .anyExchange().permitAll()  // 테스트를 위해 모든 요청 허용
            )
            .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
            .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
            .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(
            "http://localhost:3000",    // 프론트엔드 주소
            "http://localhost:8000"     // 게이트웨이 주소
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization", 
            "Content-Type", 
            "X-Auth-Token",
            "X-Authorization-Id",
            "X-Authorization-Role"
        ));
        configuration.setExposedHeaders(Arrays.asList(
            "X-Auth-Token",
            "X-Authorization-Id",
            "X-Authorization-Role"
        ));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
} 