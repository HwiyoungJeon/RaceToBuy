package com.example.racetobuy.service.member;

import com.example.racetobuy.domain.member.dto.MemberSignupRequest;
import com.example.racetobuy.domain.member.entity.Member;
import com.example.racetobuy.domain.member.entity.MemberSecureData;
import com.example.racetobuy.domain.member.repository.MemberRepository;
import com.example.racetobuy.domain.member.repository.MemberSecureDataRepository;
import com.example.racetobuy.global.constant.ErrorCode;
import com.example.racetobuy.global.util.AESUtil;
import com.example.racetobuy.global.util.ApiResponse;
import com.example.racetobuy.global.util.SHA256Util;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final MemberRepository memberRepository;
    private final MemberSecureDataRepository memberSecureDataRepository;
    private final AESUtil aesUtil;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public ApiResponse<?> signup(MemberSignupRequest request) {

        String emailHash = SHA256Util.hash(request.getEmail());

        boolean isEmailExists = memberSecureDataRepository.existsByEmailHash(emailHash);
        if (isEmailExists) {
            return ApiResponse.createException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        // 데이터 암호화
        String encryptedEmail = aesUtil.encrypt(request.getEmail());
        String encryptedName = aesUtil.encrypt(request.getUsername());
        String encryptedAddress = aesUtil.encrypt(request.getAddress());
        String encryptedPassword = passwordEncoder.encode(request.getPassword());

        // 회원 엔티티 생성 및 저장
        Member member = new Member(encryptedName, encryptedEmail, encryptedPassword, request.getPhoneNumber(), encryptedAddress);
        memberRepository.save(member);

        // MemberSecureData 생성 및 저장
        MemberSecureData memberSecureData = new MemberSecureData(
                member,
                emailHash,
                encryptedPassword,
                SHA256Util.hash(request.getUsername()),
                SHA256Util.hash(request.getAddress())
        );
        memberSecureDataRepository.save(memberSecureData);

        // 응답 반환
        return ApiResponse.createSuccess();
    }
}
