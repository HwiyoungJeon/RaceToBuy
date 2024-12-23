package com.example.racetobuy.global.config;

import com.example.racetobuy.global.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@EnableWebSecurity
@Configuration
@RequiredArgsConstructor // 설정 클래스
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean  // BCryptPasswordEncoder를 Bean으로 등록
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.csrf(AbstractHttpConfigurer::disable);
        httpSecurity.httpBasic(AbstractHttpConfigurer::disable);
        httpSecurity.headers(c1 -> c1.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin));
        httpSecurity.cors(c -> c.configurationSource(configurationSource()));
        httpSecurity.sessionManagement(c -> c.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        httpSecurity.formLogin(AbstractHttpConfigurer::disable);

        httpSecurity.authorizeHttpRequests((request) -> request
                .requestMatchers(new AntPathRequestMatcher("/auth/**"),
                        new AntPathRequestMatcher("/login/**")
                ).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/refresh"))
                .access((authentication, context) -> {
                    // 헤더에서 Refresh-Token 가져오기
                    String refreshToken = context.getRequest().getHeader("Refresh-Token");

                    // Refresh-Token 검증 로직 (단순히 null 체크 예시)
                    if (refreshToken != null && !refreshToken.trim().isEmpty()) {
                        return new AuthorizationDecision(true); // 접근 허용
                    } else {
                        return new AuthorizationDecision(false); // 접근 거부
                    }
                })
                .anyRequest().authenticated());

        httpSecurity.logout(httpSecurityLogoutConfigurer -> httpSecurityLogoutConfigurer.disable());

        httpSecurity.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return httpSecurity.build();
    }

    @Bean
    public CorsConfigurationSource configurationSource() {

        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedHeader("*");
        configuration.addAllowedMethod("*"); // GET, POST, PUT, DELETE (Javascript 요청 허용)
        configuration.addAllowedOriginPattern("*"); // 모든 IP 주소 허용 (프론트 앤드 IP만 허용 react)
        configuration.setAllowCredentials(true); // 클라이언트에서 쿠키 요청 허용
        configuration.addExposedHeader("Authorization"); // 옛날에는 디폴트 였다. 지금은 아닙니다.
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}