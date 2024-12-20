package com.example.racetobuy.global.exception;

import com.example.racetobuy.global.constant.ErrorCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class JwtException extends BusinessException {
    public JwtException(ErrorCode errorCode) {
        super(errorCode);
    }
}