package com.jh.userservice.domain.repository;

import com.jh.userservice.domain.entity.Member;
import com.jh.userservice.domain.entity.MemberSecureData;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberSecureDataRepository extends JpaRepository<MemberSecureData, Long> {
    boolean existsByEmailHash(String email);

    Optional<MemberSecureData> findByEmailHash(String emailHash);

    Optional<MemberSecureData> findByMember(Member member);

}
