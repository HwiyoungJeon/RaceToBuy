package com.jh.userservice.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.jh.common.constant.ErrorCode;
import com.jh.common.constant.RoleToken;
import com.jh.common.exception.BusinessException;
import com.jh.common.util.ApiResponse;
import com.jh.common.util.SHA256Util;
import com.jh.userservice.domain.dto.MemberSignupRequest;
import com.jh.userservice.domain.dto.UpdatePasswordRequest;
import com.jh.userservice.domain.entity.Member;
import com.jh.userservice.domain.entity.MemberSecureData;
import com.jh.userservice.domain.entity.RefreshToken;
import com.jh.userservice.domain.repository.MemberRepository;
import com.jh.userservice.domain.repository.MemberSecureDataRepository;
import com.jh.userservice.domain.repository.RefreshTokenRepository;
import com.jh.userservice.security.JwtTokenProvider;
import com.jh.userservice.util.AESUtil;
import com.jh.userservice.util.AuthenticationUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
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
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private static final String BLACKLIST_PREFIX = "BLACKLIST_TOKEN:";
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

        // RoleToken 변환
        RoleToken roleToken = request.getRoleToken();

        // 회원 엔티티 생성 및 저장
        Member member = Member.builder()
                .username(encryptedName)
                .email(encryptedEmail)
                .password(encryptedPassword)
                .phoneNumber(request.getPhoneNumber())
                .address(encryptedAddress)
                .role(roleToken)
                .build();

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

    @Override
    public ApiResponse<?> login(String email, String password, String role) {
        // 이메일 해시 생성
        String emailHash = SHA256Util.hash(email);

        //  emailHash로 MemberSecureData 테이블에서 조회
        MemberSecureData secureData = memberSecureDataRepository.findByEmailHash(emailHash)
                .orElseThrow(() -> new BusinessException(ErrorCode.EMAIL_NOT_FOUND));

        // Member 엔티티와 연결된 데이터 가져오기
        Member member = secureData.getMember();

        // 비밀번호 검증 (BCrypt 사용)
        if (!passwordEncoder.matches(password, member.getPassword())) {
            throw new BusinessException(ErrorCode.PASSWORD_INCORRECT);
        }

        RoleToken roleToken = RoleToken.findByName(role);

        // Access Token 생성
        String accessToken = JwtTokenProvider.createAccessToken(member, role);

        // Refresh Token 관리 로직 (기존의 Refresh Token 확인)
        RefreshToken existingToken = refreshTokenRepository.findByMember(member).orElse(null);

        String refreshToken;
        if (existingToken != null) {
            // 기존 Refresh Token의 만료 시간을 확인
            long remainingTime = Duration.between(Instant.now(), existingToken.getExpiration()).toSeconds();

            // 남은 시간이 1일(24 * 60 * 60 = 86400초) 이하일 때만 새로 발급
            if (remainingTime <= 86400) {
                refreshToken = jwtTokenProvider.createRefreshToken(member, role);
                existingToken.updateToken(refreshToken);
            } else {
                refreshToken = existingToken.getToken();
            }
        } else {
            // Refresh Token이 없을 때는 새로 생성
            refreshToken = jwtTokenProvider.createRefreshToken(member, role);
            RefreshToken newRefreshToken = new RefreshToken(member, refreshToken);
            refreshTokenRepository.save(newRefreshToken);
        }

        // 응답 반환 (토큰 포함)
        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken", accessToken);
        tokens.put("refreshToken", refreshToken);

        return ApiResponse.success(tokens);
    }

    /**
     * 특정 기기에서 로그아웃
     */
    @Transactional
    @Override
    public ApiResponse<?> logout(String accessToken) {
        Long memberId = AuthenticationUtil.getMemberId();
        String token = accessToken.replace("Bearer ", "");

        // 액세스 토큰의 만료 시간 가져오기
        long expiration = jwtTokenProvider.getExpiration(token);
        long remainingTime = expiration - System.currentTimeMillis();

        // redis에 블랙리스트 추가
        redisTemplate.opsForValue().set(
                BLACKLIST_PREFIX + token,
                "LOGOUT",
                Duration.ofMillis(remainingTime) // 남은 만료 시간만큼 Redis에 유지
        );

        return ApiResponse.success("로그아웃 되었습니다.");
    }

    /**
     * 모든 기기에서 로그아웃
     */
    @Transactional
    @Override
    public ApiResponse<?> logoutAllDevices(String accessToken) {
        Long memberId = AuthenticationUtil.getMemberId();

        String token = accessToken.replace("Bearer ", "");
        long expiration = jwtTokenProvider.getExpiration(token);
        long remainingTime = expiration - System.currentTimeMillis();


        redisTemplate.opsForValue().set(
                BLACKLIST_PREFIX + token,
                "LOGOUT_ALL",
                Duration.ofMillis(remainingTime)
        );

        // DB에서 리프레시 토큰 조회
        refreshTokenRepository.findByMember_MemberId(memberId).ifPresent(refreshToken -> {
            String rawToken = refreshToken.getToken();

            if (rawToken.startsWith("Bearer ")) {
                rawToken = rawToken.substring(7); // "Bearer " 이후의 부분 추출
            }
            // 리프레시 토큰의 만료 시간 가져오기
            long refreshTokenExpiration = jwtTokenProvider.getExpiration(rawToken);
            long refreshTokenRemainingTime = refreshTokenExpiration - System.currentTimeMillis();

            // Redis에 리프레시 토큰 블랙리스트 추가
            redisTemplate.opsForValue().set(
                    BLACKLIST_PREFIX + rawToken,
                    "LOGOUT_ALL_REFRESH",
                    Duration.ofMillis(refreshTokenRemainingTime) // 리프레시 토큰 남은 유효 시간만큼 Redis에 유지
            );

            // DB에서 리프레시 토큰 삭제
            refreshTokenRepository.delete(refreshToken);
        });
        return ApiResponse.success("모든 기기에서 로그아웃 되었습니다.");
    }

    /**
     * 비밀번호 변경 및 모든 기기 로그아웃
     */
    @Transactional
    @Override
    public ApiResponse<?> updatePassword(String accessToken, UpdatePasswordRequest request) {
        Long memberId = AuthenticationUtil.getMemberId();

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        MemberSecureData memberSecureData = memberSecureDataRepository.findByMember(member)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        if (!passwordEncoder.matches(request.getOldPassword(), member.getPassword())) {
            throw new BusinessException(ErrorCode.PASSWORD_INCORRECT);
        }

        if (passwordEncoder.matches(request.getNewPassword(), member.getPassword())) {
            throw new BusinessException(ErrorCode.PASSWORD_SAME_AS_OLD);
        }

        String encryptedNewPassword = passwordEncoder.encode(request.getNewPassword()); // BCrypt
        member.updatePassword(encryptedNewPassword); // Member 테이블의 비밀번호 변경

        String hashedNewPassword = SHA256Util.hash(request.getNewPassword()); // SHA-256 해시

        memberSecureData.updatePasswordHash(hashedNewPassword); // MemberSecureData 테이블의 비밀번호 해시 변경

        String token = accessToken.replace("Bearer ", "");
        long expiration = jwtTokenProvider.getExpiration(token);
        long remainingTime = expiration - System.currentTimeMillis();

        redisTemplate.opsForValue().set(
                BLACKLIST_PREFIX + token,
                "PASSWORD_CHANGE",
                Duration.ofMillis(remainingTime)
        );

        // DB에서 리프레시 토큰 조회
        refreshTokenRepository.findByMember_MemberId(memberId).ifPresent(refreshToken -> {

            String rawToken = refreshToken.getToken();
            if (rawToken.startsWith("Bearer ")) {
                rawToken = rawToken.substring(7); // "Bearer " 이후의 부분 추출
            }
            // 리프레시 토큰의 만료 시간 가져오기
            long refreshTokenExpiration = jwtTokenProvider.getExpiration(rawToken);
            long refreshTokenRemainingTime = refreshTokenExpiration - System.currentTimeMillis();

            // Redis에 리프레시 토큰 블랙리스트 추가
            redisTemplate.opsForValue().set(
                    BLACKLIST_PREFIX + rawToken,
                    "LOGOUT_ALL_REFRESH",
                    Duration.ofMillis(refreshTokenRemainingTime) // 리프레시 토큰 남은 유효 시간만큼 Redis에 유지
            );

            //모든 기기에서의 Refresh Token 삭제
            refreshTokenRepository.deleteAllByMember_MemberId(memberId);
        });


        return ApiResponse.success("비밀번호 변경 및 모든 기기 로그아웃 되었습니다.");
    }

    /**
     * 리플레쉬 토큰존재시 액세스 토큰 자동 발급
     */
    @Override
    public ApiResponse<?> refreshAccessToken(String refreshToken) {
        // 토큰에서 Bearer 제거
        refreshToken = refreshToken.replace(JwtTokenProvider.TOKEN_PREFIX, "");

        //  블랙리스트 확인
        if (isTokenBlacklisted(refreshToken)) {
            throw new BusinessException(ErrorCode.REFRESH_TOKEN_BLACKLISTED);
        }

        // 3리프레시 토큰 유효성 검증
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new BusinessException(ErrorCode.JWT_INVALID);
        }

        // 리프레시 토큰에서 사용자 정보 추출
        Long memberId = jwtTokenProvider.getMemberIdFromToken(refreshToken);
        String role = jwtTokenProvider.getRoleFromToken(refreshToken);

        //  사용자 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        //  새로운 액세스 토큰 생성
        String newAccessToken = jwtTokenProvider.createAccessToken(member, role);

        // 7응답 반환
        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken", newAccessToken);
        tokens.put("refreshToken", JwtTokenProvider.TOKEN_PREFIX + refreshToken);

        return ApiResponse.success(tokens);
    }


    private boolean isTokenBlacklisted(String token) {
        String blacklistKey = BLACKLIST_PREFIX + token;
        return redisTemplate.opsForValue().get(blacklistKey) != null;
    }

    /**
     * Refresh Token 탈취 감지 후 블랙리스트 등록
     */
    @Transactional
    public void blacklistRefreshToken(String refreshToken) {
        long expiration = jwtTokenProvider.getExpiration(refreshToken);
        long remainingTime = expiration - System.currentTimeMillis();

        redisTemplate.opsForValue().set(
                BLACKLIST_PREFIX + refreshToken,
                "REFRESH_TOKEN_STOLEN",
                Duration.ofMillis(remainingTime)
        );
    }


}



