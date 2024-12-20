package com.example.racetobuy.service.member;

import com.example.racetobuy.domain.member.dto.MemberSignupRequest;
import com.example.racetobuy.domain.member.entity.Member;
import com.example.racetobuy.domain.member.entity.MemberSecureData;
import com.example.racetobuy.domain.member.repository.MemberRepository;
import com.example.racetobuy.domain.member.repository.MemberSecureDataRepository;
import com.example.racetobuy.global.constant.ErrorCode;
import com.example.racetobuy.global.exception.BusinessException;
import com.example.racetobuy.global.util.AESUtil;
import com.example.racetobuy.global.util.ApiResponse;
import com.example.racetobuy.global.util.SHA256Util;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final MemberRepository memberRepository;
    private final MemberSecureDataRepository memberSecureDataRepository;
    private final AESUtil aesUtil;
    private final BCryptPasswordEncoder passwordEncoder;
    private final MailService mailService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper; // ObjectMapper 추가
//    private final JwtUtil jwtUtil;

    /**
     * 이메일 인증 코드 전송
     */
    @Override
    public ApiResponse<?> sendVerificationCode(String email) {
        //  Redis에서 회원 정보가 존재하는지 확인
        String emailKey = "SIGNUP:" + email;

        Object object = redisTemplate.opsForValue().get(emailKey);
        if (object == null) {
            throw new BusinessException(ErrorCode.EMAIL_NOT_FOUND); //Redis에 존재하지 않으면 예외 발생
        }


        MemberSignupRequest memberSignupRequest;
        try {
            memberSignupRequest = objectMapper.convertValue(object, MemberSignupRequest.class);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.VERIFICATION_CODE_EXPIRED);
        }


        //  6자리 인증 코드 생성
        String verificationCode = String.format("%06d", new Random().nextInt(999999));

        // Redis에 인증 코드 저장 (5분)
        redisTemplate.opsForValue().set(
                "EMAIL_VERIFICATION_CODE:" + email,
                verificationCode,
                Duration.ofMinutes(5)
        );

        // 이메일로 인증 코드 전송
        mailService.sendEmail(
                email,
                "이메일 인증 코드",
                "<h1>이메일 인증 코드</h1>" +
                        "<p>회원가입을 위해 아래의 인증 코드를 입력하세요.</p>" +
                        "<h2>" + verificationCode + "</h2>"
        );

        return ApiResponse.success("이메일 발송 완료.");
    }

    /**
     * 이메일 인증 코드 검증
     */
    @Override
    public ApiResponse<?> verifyEmailCode(String email, String code) {
        String storedCode = (String) redisTemplate.opsForValue().get("EMAIL_VERIFICATION_CODE:" + email);

        if (storedCode == null) {
            throw new BusinessException(ErrorCode.VERIFICATION_CODE_EXPIRED);
        }
        if (!storedCode.equals(code)) {
            throw new BusinessException(ErrorCode.VERIFICATION_CODE_INVALID);
        }

        // 인증에 성공했으므로 Redis에서 인증 상태 플래그를 설정
        redisTemplate.opsForValue().set("EMAIL_VERIFIED:" + email, true, Duration.ofMinutes(10));

        return ApiResponse.success("이메일 코드 검증이 완료 " + "회원가입을 진행해 주세요.");
    }

    /**
     * 회원가입 요청
     */
    @Override
    @Transactional
    public ApiResponse<?> signup(MemberSignupRequest request) {
        String emailHash = SHA256Util.hash(request.getEmail());

        boolean isEmailExists = memberSecureDataRepository.existsByEmailHash(emailHash);
        if (isEmailExists) {
            return ApiResponse.createException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }
        //인증이 완료되지 않았으면 회원가입 불가능
        Boolean isVerified = (Boolean) redisTemplate.opsForValue().get("EMAIL_VERIFIED:" + request.getEmail());
        if (isVerified == null || !isVerified) {
            throw new BusinessException(ErrorCode.EMAIL_NOT_VERIFIED);
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


        // 인증 상태 제거 (이미 가입이 완료되었기 때문에)
        String emailKey1 = "EMAIL_VERIFIED:" + request.getEmail().trim().toLowerCase();
        String emailKey2 = "SIGNUP:" + request.getEmail().trim().toLowerCase();
        String emailKey3 = "EMAIL_VERIFICATION_CODE:" + request.getEmail().trim().toLowerCase();
        System.out.println("Trying to delete keys:" + "emailKey1 := " + emailKey1 + "emailKey2 :=" + emailKey2 + "emailKey3 :=" + emailKey3);
        redisTemplate.delete(emailKey1);
        redisTemplate.delete(emailKey2);
        redisTemplate.delete(emailKey3);
        return ApiResponse.createSuccess();
    }
}



