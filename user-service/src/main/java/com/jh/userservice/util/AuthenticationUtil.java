package com.jh.userservice.util;

import com.jh.common.constant.ErrorCode;
import com.jh.common.exception.BusinessException;
import com.jh.userservice.domain.entity.Member;
import com.jh.userservice.security.MemberDetails;
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
