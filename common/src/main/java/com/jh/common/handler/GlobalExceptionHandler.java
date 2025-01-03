package com.jh.common.handler;


import com.jh.common.constant.ErrorCode;
import com.jh.common.exception.BusinessException;
import com.jh.common.util.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;


@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<ApiResponse<?>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.error("handleMethodArgumentNotValidException", e);
        final ApiResponse<?> response = ApiResponse.createValidationFail(e.getBindingResult());

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(BindException.class)
    protected ResponseEntity<ApiResponse<?>> handleBindException(BindException e) {
        log.error("handleBindException", e);
        final ApiResponse<?> response = ApiResponse.createException(HttpStatus.BAD_REQUEST.value(), e.getMessage());

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    protected ResponseEntity<ApiResponse<String>> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException e) {
        log.error("handleMethodArgumentTypeMismatchException", e);
        final ApiResponse<String> response = ApiResponse.createException(HttpStatus.BAD_REQUEST.value(),
                e.getMessage());

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AccessDeniedException.class)
    protected ResponseEntity<ApiResponse<String>> handleAccessDeniedException(AccessDeniedException e) {
        log.error("handleAccessDeniedException", e);
        final ApiResponse<String> response = ApiResponse.createException(ErrorCode.ACCESS_DENIED);

        return new ResponseEntity<>(response, ErrorCode.ACCESS_DENIED.getHttpStatus());
    }

    @ExceptionHandler(BusinessException.class)
    protected ResponseEntity<ApiResponse<?>> handleBusinessException(BusinessException e) {
        log.error("handleBusinessException", e);
        final ApiResponse<?> response = ApiResponse.createException(e.getCode(), e.getMessage());

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ApiResponse<String>> handleException(Exception e) {
        log.error("handleException", e);
        final ApiResponse<String> response = ApiResponse.createException(ErrorCode.INTERNAL_SERVER_ERROR);

        return new ResponseEntity<>(response, ErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus());
    }
}