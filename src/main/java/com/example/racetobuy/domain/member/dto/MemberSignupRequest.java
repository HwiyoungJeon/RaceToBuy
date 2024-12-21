package com.example.racetobuy.domain.member.dto;


import com.example.racetobuy.global.constant.RoleToken;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class MemberSignupRequest {

    @NotBlank(message = "이름은 필수 입력값입니다.")
    @Size(min = 2, max = 50, message = "이름은 2~50자 사이여야 합니다.")
    private String username;

    @NotBlank(message = "이메일은 필수 입력값입니다.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    private String email;

    @NotBlank(message = "비밀번호는 필수 입력값입니다.")
    @Size(min = 8, max = 100, message = "비밀번호는 8~100자 사이여야 합니다.")
    private String password;

    @NotBlank(message = "주소는 필수 입력값입니다.")
    private String address;

    @NotBlank(message = "전화번호는 필수 입력값입니다.")
    private String phoneNumber;

    @NotBlank(message = "룰은 필수 입력값입니다.")
    private String role;

    /**
     * Role을 Enum으로 변환하여 반환
     */
    public RoleToken getRoleToken() {
        return RoleToken.findByName(this.role);
    }
}