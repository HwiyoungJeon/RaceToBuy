package com.jh.userservice.domain.entity;

import com.jh.common.domain.timestamp.TimeStamp;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@Entity
@Table(name = "refresh_token")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken extends TimeStamp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "refresh_token_id")
    private Long refreshTokenId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false, unique = true)
    private Member member;

    @Column(name = "token", nullable = false, columnDefinition = "TEXT")
    private String token;

    @Column(name = "expiration", nullable = false)
    private Instant expiration;

    public RefreshToken(Member member, String token) {
        this.member = member;
        this.token = token;
        this.expiration = Instant.now().plusSeconds(604800);
    }

    public void updateToken(String token) {
        this.token = token;
        this.expiration = Instant.now().plusSeconds(604800);
    }
}