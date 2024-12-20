package com.example.racetobuy.controller;

import com.example.racetobuy.domain.member.dto.MemberSignupRequest;
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
    @PostMapping("/signup")
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
    @PostMapping("/send-code")
    public ApiResponse<?> sendVerificationCode(@RequestParam String email) {
        return authService.sendVerificationCode(email);
    }

    /**
     * 이메일 인증 코드 검증
     */
    @PostMapping("/verify-code")
    public ApiResponse<?> verifyEmailCode(@RequestParam String email, @RequestParam String code) {
        return authService.verifyEmailCode(email, code);
    }
}