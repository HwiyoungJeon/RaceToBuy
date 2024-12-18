package com.example.racetobuy.global.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "로그인 후 이용해주세요."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "오류가 발생했습니다. 잠시 후 다시 시도해주세요."),
    LOGIN_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "로그인에 실패했습니다. 다시 시도해주세요.");

    private final HttpStatus httpStatus;
    private final String message;

    public int getStatusCode() {
        return httpStatus.value();
    }
}
