package com.example.racetobuy.domain.member.entity;

import com.example.racetobuy.domain.order.Order;
import com.example.racetobuy.domain.timestamp.TimeStamp;
import com.example.racetobuy.domain.wishlist.entity.Wishlist;
import com.example.racetobuy.global.constant.RoleToken;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Table(name = "member")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends TimeStamp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long memberId;

    @NotBlank(message = "이름은 필수 입력 항목입니다.")
    @Column(name = "username", nullable = false, length = 50)
    private String username;

    @NotBlank(message = "이메일은 필수 입력 항목입니다.")
    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @NotBlank(message = "비밀번호는 필수 입력 항목입니다.")
    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @NotBlank(message = "전화번호는 필수 입력 항목입니다.")
    @Column(name = "phone_number", nullable = false, length = 20)
    private String phoneNumber;

    @NotBlank(message = "주소는 필수 입력 항목입니다.")
    @Column(name = "address", nullable = false, columnDefinition = "TEXT")
    private String address;

    @Enumerated(EnumType.STRING) // Enum을 String으로 매핑
    @Column(name = "role", nullable = false, length = 10)
    private RoleToken role;

    @OneToMany(mappedBy = "member")
    private List<Order> orders = new ArrayList<>();

    @OneToMany(mappedBy = "member")
    private List<Wishlist> wishlists = new ArrayList<>();

    @Builder
    public Member(Long memberId, String username, String email, String password, String phoneNumber, String address, RoleToken role) {
        this.memberId = memberId;
        this.username = username;
        this.email = email;
        this.password = password;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.role = role;
    }

    public void updatePassword(String newPassword) {
        this.password = newPassword;
    }

    public void updateRole(RoleToken newRole) { // Role 업데이트 메서드
        this.role = newRole;
    }

}