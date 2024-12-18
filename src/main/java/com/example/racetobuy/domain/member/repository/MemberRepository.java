package com.example.racetobuy.domain.member.repository;

import com.example.racetobuy.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {
}
