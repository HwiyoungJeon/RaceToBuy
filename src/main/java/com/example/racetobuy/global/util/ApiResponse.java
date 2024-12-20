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

    /**
     * 🔥 addData 메서드 (data를 Map으로 확장하여 추가 데이터 삽입)
     *
     * @param key   데이터 키 (예: accessToken)
     * @param value 데이터 값 (예: jwt 토큰 값)
     * @return ApiResponse 자기 자신을 반환하여 체이닝 가능
     */
    @SuppressWarnings("unchecked")
    public ApiResponse<Map<String, Object>> addData(String key, Object value) {
        if (!(this.data instanceof Map)) {
            Map<String, Object> newData = new HashMap<>();
            newData.put("message", this.data); // 🔥 기존 데이터가 있다면 message로 추가
            this.data = (T) newData;
        }
        ((Map<String, Object>) this.data).put(key, value);
        return (ApiResponse<Map<String, Object>>) this;
    }

}