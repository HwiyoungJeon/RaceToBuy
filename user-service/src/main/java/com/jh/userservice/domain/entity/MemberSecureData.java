package com.jh.userservice.domain.entity;

import com.jh.common.domain.timestamp.TimeStamp;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "member_secure_data")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberSecureData extends TimeStamp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_secure_data_id")
    private Long memberSecureDataId; // 기본 키 (AUTO_INCREMENT)

    @OneToOne
    @JoinColumn(name = "member_id", nullable = false, unique = true)
    private Member member; // Member 테이블과의 외래 키 관계 (1:1 관계)

    @Column(name = "email_hash", nullable = false, unique = true, length = 255)
    private String emailHash; // 이메일 해시 (고유 제약 조건)

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash; // 비밀번호 해시 (SHA 또는 BCrypt 해시값)

    @Column(name = "name_hash", nullable = false, length = 255)
    private String nameHash; // 이름 해시 (SHA-256 해시값)

    @Column(name = "address_hash", nullable = false, length = 255)
    private String addressHash; // 주소 해시 (SHA-256 해시값)

    // 엔티티 생성 메서드
    public MemberSecureData(Member member, String emailHash, String passwordHash, String nameHash, String addressHash) {
        this.member = member;
        this.emailHash = emailHash;
        this.passwordHash = passwordHash;
        this.nameHash = nameHash;
        this.addressHash = addressHash;
    }

    public void updatePasswordHash(String newPasswordHash) {
        this.passwordHash = newPasswordHash;
    }
}