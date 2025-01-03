package com.jh.userservice.security;

import com.jh.userservice.domain.entity.Member;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Getter
public class MemberDetails implements UserDetails {

    private final Member member;

    public MemberDetails(Member member) {
        this.member = member;
    }

    public Member getMember() {
        return member;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 권한을 반환합니다. 예를 들어, ADMIN, USER 등의 권한을 반환할 수 있습니다.
        return Collections.emptyList(); // 필요에 따라 권한 추가 가능
    }

    @Override
    public String getPassword() {
        return member.getPassword();
    }

    @Override
    public String getUsername() {
        return member.getEmail(); // 보통 이메일을 username으로 사용
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // 계정 만료 여부 (true면 만료되지 않음)
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // 계정 잠금 여부 (true면 잠금되지 않음)
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // 자격 증명(비밀번호) 만료 여부 (true면 만료되지 않음)
    }

    @Override
    public boolean isEnabled() {
        return true; // 계정 활성화 여부 (true면 활성화됨)
    }

    // 추가적인 사용자 정보를 가져오는 메서드
    public Long getMemberId() {
        return member.getMemberId();
    }

    public String getEmail() {
        return member.getEmail();
    }

    public String getUsernameCustom() {
        return member.getUsername();
    }
}
