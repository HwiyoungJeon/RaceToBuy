package com.example.racetobuy.domain.member.repository;

import com.example.racetobuy.domain.member.entity.Member;
import com.example.racetobuy.domain.member.entity.MemberSecureData;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberSecureDataRepository extends JpaRepository<MemberSecureData, Long> {
    boolean existsByEmailHash(String email);
    Optional<MemberSecureData> findByEmailHash(String emailHash);
    Optional<MemberSecureData> findByMember(Member member);

}
