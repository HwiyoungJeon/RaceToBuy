package com.example.racetobuy.service.member;

import com.example.racetobuy.domain.member.dto.MemberSignupRequest;
import com.example.racetobuy.global.util.ApiResponse;

public interface AuthService {
    ApiResponse<?> signup(MemberSignupRequest request);
}
