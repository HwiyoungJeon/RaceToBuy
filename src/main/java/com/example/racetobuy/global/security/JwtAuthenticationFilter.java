package com.example.racetobuy.global.security;

import com.example.racetobuy.domain.member.entity.Member;
import com.example.racetobuy.global.constant.ErrorCode;
import com.example.racetobuy.global.constant.RoleToken;
import com.example.racetobuy.global.exception.BusinessException;
import com.example.racetobuy.global.util.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws IOException, ServletException {

        //요청 헤더에서 JWT 토큰 추출
        String jwt = request.getHeader(JwtTokenProvider.HEADER);
        if (!StringUtils.hasText(jwt)) {
            filterChain.doFilter(request, response);
            return;
        }

        jwt = jwt.replace(JwtTokenProvider.TOKEN_PREFIX, "");

        // Redis 블랙리스트 확인
        if (isTokenBlacklisted(jwt)) {
            log.error("JWT Token is blacklisted: {}", jwt);
            setErrorResponse(response, new BusinessException(ErrorCode.JWT_INVALID));
            return;
        }

        try {
            // JWT 토큰 검증
            if (!jwtTokenProvider.validateToken(jwt)) {
                filterChain.doFilter(request, response);
                return;
            }

            // 토큰 타입 확인
            String tokenType = jwtTokenProvider.getTokenType(jwt);

            if ("REFRESH".equals(tokenType)) {
                // 리프레시 토큰은 별도의 처리로직
                processRefreshToken(jwt);
                filterChain.doFilter(request, response); // 리프레시 토큰 처리 후 다음 필터로 전달
                return;
            } else if ("ACCESS".equals(tokenType)) {
                // 액세스 토큰 처리
                try {
                    processAccessToken(jwt, request);
                }catch (Exception e){
                    log.error("Authentication Error: {}", e.getMessage());
                    setErrorResponse(response, new BusinessException(ErrorCode.JWT_INVALID));
                    return;
                }
            } else {
                log.error("Invalid Token Type: {}", tokenType);
                setErrorResponse(response, new BusinessException(ErrorCode.JWT_INVALID));
                return;
            }

        } catch (JwtException e) {
            log.error("JWT Exception Occurred: {}", e.getMessage());
            setErrorResponse(response, new BusinessException(ErrorCode.JWT_INVALID));
            return;
        }


        // 다음 필터로 요청 전달
        filterChain.doFilter(request, response);
    }

    /**
     * 🔥 에러 응답을 설정하는 메서드
     *
     * @param servletResponse 응답 객체
     * @param e BusinessException 예외
     * @throws IOException 입출력 예외 발생 시
     */
    private void setErrorResponse(HttpServletResponse servletResponse, BusinessException e) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        ApiResponse<String> response = ApiResponse.createException(e.getCode(), e.getMessage());

        servletResponse.setStatus(e.getCode());
        servletResponse.setContentType("application/json; charset=utf-8");
        String body = mapper.writeValueAsString(response);
        servletResponse.getOutputStream().write(body.getBytes());
    }

    private boolean isTokenBlacklisted(String jwt) {
        String blacklistKey = "BLACKLIST_TOKEN:" + jwt;
        return redisTemplate.opsForValue().get(blacklistKey) != null;
    }

    // 액세스 토큰 처리
    private void processAccessToken(String jwt, HttpServletRequest request) {
        Long memberId = jwtTokenProvider.getMemberIdFromToken(jwt);
        String email = jwtTokenProvider.getEmailFromToken(jwt);
        String role = jwtTokenProvider.getRoleFromToken(jwt);

        RoleToken roleToken = RoleToken.findByName(role);

        Member member = Member.builder()
                .memberId(memberId)
                .email(email)
                .role(roleToken)
                .build();

        MemberDetails memberDetails = new MemberDetails(member);

        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(memberDetails, null, memberDetails.getAuthorities());

        authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    }

    // 리프레시 토큰 처리
    private void processRefreshToken(String jwt) {
        // 리프레시 토큰은 인증 컨텍스트에 추가하지 않고 단순히 유효성을 확인하는 수준에서 처리
        if (!jwtTokenProvider.validateRefreshToken(jwt)) {
            throw new BusinessException(ErrorCode.JWT_INVALID);
        }
        // 블랙리스트 확인
        if (isTokenBlacklisted(jwt)) {
            log.error("Refresh Token is blacklisted: {}", jwt);
            throw new BusinessException(ErrorCode.REFRESH_TOKEN_BLACKLISTED);
        }

        // 리프레시 토큰 처리 로직 추가 (필요 시 확장)
        log.info("Refresh Token is valid: {}", jwt);
    }
}
