package com.example.racetobuy.global.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    // 인증 관련 에러 코드
    VERIFICATION_CODE_INVALID(HttpStatus.BAD_REQUEST, "인증 코드가 잘못되었습니다."),
    VERIFICATION_CODE_EXPIRED(HttpStatus.BAD_REQUEST, "인증 코드가 만료되었습니다."),
    EMAIL_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 이메일입니다."),
    EMAIL_NOT_VERIFIED(HttpStatus.FORBIDDEN, "이메일 인증이 필요합니다."),
    EMAIL_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "이미 존재하는 이메일입니다."),
    EMAIL_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "이메일 전송에 실패했습니다."),

    // 로그인, 인증 관련 에러 코드
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "로그인 후 이용해주세요."),
    LOGIN_FAILED(HttpStatus.BAD_REQUEST, "로그인에 실패했습니다. 다시 시도해주세요."),
    JWT_INVALID(HttpStatus.BAD_REQUEST, "인증 정보가 올바르지 않습니다."),
    JWT_EXPIRED(HttpStatus.UNAUTHORIZED, "인증 정보가 만료되었습니다."),

    // 서버 관련 에러 코드
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버에 오류가 발생했습니다. 잠시 후 다시 시도해주세요."),
    UNKNOWN_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "알 수 없는 오류가 발생했습니다.");

    private final HttpStatus httpStatus;
    private final String message;

    public int getStatusCode() {
        return httpStatus.value();
    }
}
