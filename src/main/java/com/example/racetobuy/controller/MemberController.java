package com.example.racetobuy.controller;

import com.example.racetobuy.domain.member.dto.MemberSignupRequest;
import com.example.racetobuy.global.util.ApiResponse;
import com.example.racetobuy.service.member.AuthService;
import com.example.racetobuy.service.member.AuthServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final AuthService authService;

    /**
     * 회원가입 요청을 처리합니다.
     * @param request 사용자 회원가입 요청 정보
     * @return ApiResponse 성공 또는 실패 응답
     */
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse> signup(@Valid @RequestBody MemberSignupRequest request) {
        ApiResponse response = authService.signup(request);
        return ResponseEntity.ok(response);
    }
}