package com.example.racetobuy.global.security;

import com.example.racetobuy.domain.member.entity.Member;
import com.example.racetobuy.global.constant.ErrorCode;
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

        try {
            // JWT 토큰 검증
            if (!jwtTokenProvider.validateToken(jwt)) {
                filterChain.doFilter(request, response);
                return;
            }

        } catch (JwtException e) {
            log.error("JWT Exception Occurred: {}", e.getMessage());
            setErrorResponse(response, new BusinessException(ErrorCode.JWT_INVALID));
            return;
        }

        // JWT에서 사용자 정보 추출
        try {
            Long memberId = jwtTokenProvider.getMemberIdFromToken(jwt);
            String email = jwtTokenProvider.getEmailFromToken(jwt);

            //Member 객체 생성
            Member member = Member.builder()
                    .memberId(memberId)
                    .email(email)
                    .build();

            MemberDetails memberDetails = new MemberDetails(member);

            //Spring Security에 인증 정보 설정
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(memberDetails, null, memberDetails.getAuthorities());

            authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);

        } catch (Exception e) {
            log.error("Authentication Error: {}", e.getMessage());
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
}
