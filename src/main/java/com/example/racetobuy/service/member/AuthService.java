package com.example.racetobuy.service.member;

import com.example.racetobuy.domain.member.dto.MemberSignupRequest;
import com.example.racetobuy.domain.member.dto.UpdatePasswordRequest;
import com.example.racetobuy.global.util.ApiResponse;

public interface AuthService {
    /**
     * 회원가입 메서드
     *
     * @param request 회원가입 요청 정보
     * @return ApiResponse 성공 또는 실패 응답
     */
    ApiResponse<?> signup(MemberSignupRequest request);

    /**
     * 이메일 인증 코드를 전송하는 메서드
     *
     * @param email 이메일 주소
     * @return ApiResponse 성공 또는 실패 응답
     */
    ApiResponse<?> sendVerificationCode(String email);

    /**
     * 이메일 인증 코드를 검증하는 메서드
     *
     * @param email 이메일 주소
     * @param code 인증 코드
     * @return ApiResponse 성공 또는 실패 응답
     */
    ApiResponse<?> verifyEmailCode(String email, String code);

    /**
     * 로그인 메서드
     *
     * @param email    이메일
     * @param password 비밀번호
     * @return JWT 토큰을 담은 응답
     */
    ApiResponse<?> login(String email, String password, String role);

    /**
     * 로그아웃 메서드 (현재 기기에서만 로그아웃)
     *
     * @param accessToken 토큰
     * @return 성공 응답
     */
     ApiResponse<?> logout(String accessToken);

    /**
     * 비밀번호 변경 및 모든 기기 로그아웃 메서드
     *
     * @param accessToken  토큰
     * @param request 기존 비밀번호
     * @param request 새 비밀번호
     * @return 성공 응답
     */
    ApiResponse<?> updatePassword(String accessToken, UpdatePasswordRequest request);

    /**
     * 모든 기기에서 로그아웃 메서드
     *
     * @param accessToken  토큰
     * @return 성공 응답
     */
    ApiResponse<?> logoutAllDevices(String accessToken);

    /**
     * 리플레쉬 토큰존재시 액세스 토큰 자동 발급
     *
     * @param refreshToken  토큰
     * @return 성공 응답
     */
    ApiResponse<?> refreshAccessToken(String refreshToken);
}
