package com.example.racetobuy.domain.member.repository;

import com.example.racetobuy.domain.member.entity.Member;
import com.example.racetobuy.domain.member.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByMember(Member member);

    //특정 회원의 모든 기기의 Refresh Token 삭제
    @Modifying
    @Transactional
    void deleteAllByMember_MemberId(Long memberId);

    /**
     * Member ID로 리프레시 토큰 조회
     *
     * @param memberId 회원 ID
     * @return 리프레시 토큰(Optional)
     */
    Optional<RefreshToken> findByMember_MemberId(Long memberId);
}

