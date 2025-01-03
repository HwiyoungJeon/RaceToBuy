package com.jh.userservice.domain.dto;

import com.jh.userservice.domain.entity.Member;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberResponse {
    private Long id;
    private String email;
    private String name;
    private String role;

    @Builder
    public MemberResponse(Long id, String email, String name, String role) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.role = role;
    }

    public static MemberResponse from(Member member) {
        return MemberResponse.builder()
                .id(member.getMemberId())
                .email(member.getEmail())
                .name(member.getUsername())
                .role(member.getRole().name())
                .build();
    }
}
