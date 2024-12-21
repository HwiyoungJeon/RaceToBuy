package com.example.racetobuy.controller;

import com.example.racetobuy.domain.member.dto.MemberSignupRequest;
import com.example.racetobuy.domain.member.dto.UpdatePasswordRequest;
import com.example.racetobuy.global.constant.ErrorCode;
import com.example.racetobuy.global.exception.BusinessException;
import com.example.racetobuy.global.util.ApiResponse;
import com.example.racetobuy.service.member.AuthService;
import com.example.racetobuy.service.member.AuthServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class MemberController {

    private final AuthService authService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    /**
     * 회원가입 요청을 처리합니다.
     * @param request 사용자 회원가입 요청 정보
     * @return ApiResponse 성공 또는 실패 응답
     */
    @PostMapping("/auth/signup")
    public ResponseEntity<ApiResponse<?>> signup(@Valid @RequestBody MemberSignupRequest request) {
        //기존의 objectMapper.writeValueAsString() 삭제
        String emailKey = "SIGNUP:" + request.getEmail();

        // request 객체 그대로 저장 (직렬화 불필요)
        redisTemplate.opsForValue().set(emailKey, request, Duration.ofMinutes(10)); // 10분 동안 유지

        ApiResponse<?> response = authService.signup(request);
        return ResponseEntity.ok(response);
    }


    /**
     * 이메일 인증 코드 전송
     */
    @PostMapping("/auth/send-code")
    public ApiResponse<?> sendVerificationCode(@RequestParam String email) {
        return authService.sendVerificationCode(email);
    }

    /**
     * 이메일 인증 코드 검증
     */
    @PostMapping("/auth/verify-code")
    public ApiResponse<?> verifyEmailCode(@RequestParam String email, @RequestParam String code) {
        return authService.verifyEmailCode(email, code);
    }

    /**
     * 로그인 JWT토큰 발급
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<?>> login(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String password = request.get("password");
        String role = request.get("role");

        if (email == null || password == null || role == null) {
            throw new BusinessException(ErrorCode.VERIFICATION_CODE_INVALID);
        }

        ApiResponse<?> response = authService.login(email, password, role);
        return ResponseEntity.ok(response);
    }

    /**
     * 특정 기기에서 로그아웃
     *
     * @param accessToken Access Token
     * @return ApiResponse
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<?>> logout(@RequestHeader("Authorization") String accessToken) {
        String token = accessToken.replace("Bearer ", ""); // Bearer 제거
        ApiResponse<?> response = authService.logout(token);
        return ResponseEntity.ok(response);
    }

    /**
     * 모든 기기에서 로그아웃
     *
     * @param accessToken Access Token
     * @return ApiResponse
     */
    @PostMapping("/logout-all")
    public ResponseEntity<ApiResponse<?>> logoutAllDevices(@RequestHeader("Authorization") String accessToken) {
        String token = accessToken.replace("Bearer ", ""); // Bearer 제거
        ApiResponse<?> response = authService.logoutAllDevices(token);
        return ResponseEntity.ok(response);
    }

    /**
     * 비밀번호 변경 및 모든 기기에서 로그아웃
     *
     * @param accessToken Access Token
     * @param request 기존 비밀번호
     * @param request 새 비밀번호
     * @return ApiResponse
     */
    @PostMapping("/update-password")
    public ResponseEntity<ApiResponse<?>> updatePassword(@RequestHeader("Authorization") String accessToken,
                                                         @RequestBody UpdatePasswordRequest request) {
        String token = accessToken.replace("Bearer ", ""); // Bearer 제거
        ApiResponse<?> response = authService.updatePassword(token, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ApiResponse<?> refreshAccessToken(@RequestHeader("Refresh-Token") String refreshToken) {
        return authService.refreshAccessToken(refreshToken);
    }

}