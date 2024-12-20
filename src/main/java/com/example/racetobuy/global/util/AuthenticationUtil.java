package com.example.racetobuy.global.util;

import com.example.racetobuy.domain.member.entity.Member;
import com.example.racetobuy.global.constant.ErrorCode;
import com.example.racetobuy.global.exception.BusinessException;
import com.example.racetobuy.global.security.MemberDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class AuthenticationUtil {

    //  로그인한 사용자의 memberId 가져오기
    public static Long getMemberId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException(ErrorCode.AUTHENTICATION_REQUIRED);
        }
        MemberDetails memberDetails = (MemberDetails) authentication.getPrincipal();
        return memberDetails.getMember().getMemberId();
    }

    // 로그인한 사용자의 MemberDetails 가져오기
    public static MemberDetails getMemberDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException(ErrorCode.AUTHENTICATION_REQUIRED);
        }
        return (MemberDetails) authentication.getPrincipal();
    }

    //  로그인한 사용자의 Member 가져오기
    public static Member getMember() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException(ErrorCode.AUTHENTICATION_REQUIRED);
        }
        MemberDetails memberDetails = (MemberDetails) authentication.getPrincipal();
        return memberDetails.getMember();
    }
}
