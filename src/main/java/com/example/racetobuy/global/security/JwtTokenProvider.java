package com.example.racetobuy.global.security;

import com.example.racetobuy.domain.member.entity.Member;
import com.example.racetobuy.global.constant.ErrorCode;
import com.example.racetobuy.global.constant.RoleToken;
import com.example.racetobuy.global.exception.JwtException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.SignatureException;
import java.util.Date;


@Component
public class JwtTokenProvider {
    public static final Long REFRESH_EXP = 1000L * 60 * 60 * 24 * 7;
    public static final Long ACCESS_EXP = 1000L * 60 * 60 * 24;
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String HEADER = "Authorization";

    private static String secretKey;

    @Value("${jwt.secret}")
    private String jwtSecret; // 인스턴스 변수로 주입

    @PostConstruct
    public void init() {
        secretKey = this.jwtSecret; // 정적 변수에 값 할당
    }

    public static String createAccessToken(Member member, String roleName) {
        RoleToken roleToken = RoleToken.findByName(roleName); // RoleToken 검증

        Claims claims = Jwts.claims();
        claims.put("id", member.getMemberId());
        claims.put("email", member.getEmail());
        claims.put("role", roleToken.getName()); // 역할 추가
        claims.put("type", "ACCESS");

        String jwt = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_EXP))
                .signWith(SignatureAlgorithm.HS256, secretKey) // secretKey로 서명
                .compact();

        return TOKEN_PREFIX + jwt;
    }

    /**
     * 🔥 RefreshToken 생성 메서드
     */
    public String createRefreshToken(Member member, String role) {
        Claims claims = Jwts.claims();
        claims.put("id", member.getMemberId());
        claims.put("email", member.getEmail());
        claims.put("role", role);
        claims.put("type", "REFRESH");

        String jwt = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_EXP)) // 만료 시간: 7일
                .signWith(SignatureAlgorithm.HS256, secretKey) // HMAC-SHA256으로 서명
                .compact();

        return TOKEN_PREFIX + jwt;
    }

    public static boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            throw new JwtException(ErrorCode.JWT_EXPIRED);
        } catch (Exception e) {
            throw new JwtException(ErrorCode.JWT_INVALID);
        }
    }


    public static String getTokenFromHeader(String header) {
        if (header != null && header.startsWith(TOKEN_PREFIX)) {
            return header.substring(TOKEN_PREFIX.length()); //  'Bearer ' 이후의 JWT만 추출
        }
        throw new RuntimeException("토큰이 유효하지 않습니다.");
    }

    public Long getMemberIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token.replace(TOKEN_PREFIX, ""))
                .getBody();

        // 🔥 JWT Claims에서 "id"를 Long 타입으로 가져옴
        return claims.get("id", Long.class);
    }


    public String getEmailFromToken(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token.replace(TOKEN_PREFIX, ""))
                .getBody();
        return claims.get("email", String.class);
    }

    // 액세스 토큰의 만료 시간 (만료 시간 반환)
    public long getExpiration(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims.getExpiration().getTime(); // 만료 시간 반환
        } catch (Exception e) {
            System.out.println("Failed to parse JWT: {}" + e.getMessage());
            throw new JwtException(ErrorCode.JWT_INVALID);
        }
    }

    public String getRoleFromToken(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody();
        return claims.get("role", String.class);
    }

    // 토큰 타입 반환
    public String getTokenType(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.get("type", String.class); // "type" 클레임에서 값 추출
    }

    // Refresh Token 유효성 확인
    public boolean validateRefreshToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            // "type" 클레임이 "REFRESH"인지 확인
            String type = claims.get("type", String.class);
            if (!"REFRESH".equals(type)) {
                return false;
            }

            // 토큰 만료 여부 확인
            Date expiration = claims.getExpiration();
            if (expiration.before(new Date())) {
                return false; // 토큰이 만료됨
            }
            return true;
        } catch (JwtException e) {
            System.out.println("Invalid Refresh Token: {}" + e.getMessage());
            return false; // 서명 오류
        } catch (Exception e) {
            System.out.println("Refresh Token Validation Error: {}" + e.getMessage());
            return false; // 기타 오류
        }
    }


}
