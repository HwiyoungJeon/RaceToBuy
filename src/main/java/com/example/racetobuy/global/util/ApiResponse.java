package com.example.racetobuy.global.util;

import com.example.racetobuy.global.constant.ErrorCode;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ApiResponse<T> {
    private static final String SUCCESS_MESSAGE = "정상적으로 처리되었습니다.";
    private int code;
    private String message;
    private T data;

    public static ApiResponse createSuccess() {
        return new ApiResponse(HttpStatus.CREATED.value(), SUCCESS_MESSAGE, null);
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(HttpStatus.OK.value(), SUCCESS_MESSAGE, data);
    }

    public static ApiResponse successWithNoData() {
        return new ApiResponse(HttpStatus.OK.value(), SUCCESS_MESSAGE, null);
    }

    public static ApiResponse<?> createValidationFail(BindingResult bindingResult) {
        Map<String, String> errors = new HashMap<>();

        List<ObjectError> allErrors = bindingResult.getAllErrors();
        for (ObjectError error : allErrors) {
            if (error instanceof FieldError) {
                errors.put(((FieldError)error).getField(), error.getDefaultMessage());
            } else {
                errors.put(error.getObjectName(), error.getDefaultMessage());
            }
        }

        return new ApiResponse<>(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.name(), errors);
    }

    public static ApiResponse<String> createException(int code, String message) {
        return new ApiResponse<>(code, message, null);
    }

    public static ApiResponse<String> createException(ErrorCode errorCode) {
        return new ApiResponse<>(errorCode.getStatusCode(), errorCode.getMessage(), null);
    }
}