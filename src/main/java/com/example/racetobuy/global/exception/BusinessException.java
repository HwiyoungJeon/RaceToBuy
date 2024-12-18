package com.example.racetobuy.global.exception;


import com.example.racetobuy.global.constant.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BusinessException extends RuntimeException {
    private HttpStatus httpStatus;
    private int code;
    private String message;

    public BusinessException(ErrorCode errorCode) {
        this.httpStatus = errorCode.getHttpStatus();
        this.code = errorCode.getStatusCode();
        this.message = errorCode.getMessage();
    }
}