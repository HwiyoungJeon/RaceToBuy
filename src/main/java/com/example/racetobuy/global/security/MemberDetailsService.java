package com.example.racetobuy.global.security;

import com.example.racetobuy.domain.member.entity.Member;
import com.example.racetobuy.domain.member.entity.MemberSecureData;
import com.example.racetobuy.domain.member.repository.MemberRepository;
import com.example.racetobuy.domain.member.repository.MemberSecureDataRepository;
import com.example.racetobuy.global.constant.ErrorCode;
import com.example.racetobuy.global.exception.BusinessException;
import com.example.racetobuy.global.util.SHA256Util;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberDetailsService implements UserDetailsService {

    private final MemberSecureDataRepository memberSecureDataRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        //이메일 해시로 MemberSecureData를 조회합니다.
        String emailHash = SHA256Util.hash(email); // 이메일을 해시로 변환
        MemberSecureData memberSecureData = memberSecureDataRepository.findByEmailHash(emailHash)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND)); //
        //MemberSecureData에서 Member 엔티티를 가져옵니다.
        Member member = memberSecureData.getMember();

        // 조회된 Member 객체를 MemberDetails로 변환합니다.
        return new MemberDetails(member);
    }
}
