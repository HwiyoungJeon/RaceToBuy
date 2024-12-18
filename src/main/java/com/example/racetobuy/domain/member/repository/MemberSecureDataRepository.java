package com.example.racetobuy.domain.member.repository;

import com.example.racetobuy.domain.member.entity.MemberSecureData;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberSecureDataRepository extends JpaRepository<MemberSecureData, Long> {
    boolean existsByEmailHash(String email);
}
