package com.jh.common.constant;

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
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "사용자 인증 실패"),

    // 로그인, 인증 관련 에러 코드
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "로그인 후 이용해주세요."),
    LOGIN_FAILED(HttpStatus.BAD_REQUEST, "로그인에 실패했습니다. 다시 시도해주세요."),
    JWT_INVALID(HttpStatus.BAD_REQUEST, "인증 정보가 올바르지 않습니다."),
    JWT_EXPIRED(HttpStatus.UNAUTHORIZED, "인증 정보가 만료되었습니다."),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "회원 정보를 찾을 수 없습니다."),
    PASSWORD_SAME_AS_OLD(HttpStatus.BAD_REQUEST, "새로운 비밀번호는 기존 비밀번호와 같을 수 없습니다."),
    AUTHENTICATION_REQUIRED(HttpStatus.UNAUTHORIZED, "인증 정보가 필요합니다."),
    REFRESH_TOKEN_BLACKLISTED(HttpStatus.UNAUTHORIZED, "리프레시 토큰이 블랙리스트에 등록되었습니다."),



    // 권한 관련 에러 코드
    INVALID_ROLE(HttpStatus.BAD_REQUEST, "유효하지 않은 권한입니다."),
    PASSWORD_INCORRECT(HttpStatus.BAD_REQUEST, "비밀번호가 틀렸습니다."),

    // 서버 관련 에러 코드
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버에 오류가 발생했습니다. 잠시 후 다시 시도해주세요."),
    UNKNOWN_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "알 수 없는 오류가 발생했습니다."),

    //상품 관련 에러 코드
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "상품을 찾을 수 없습니다."),


    // 위시리스트 관련 에러
    WISHLIST_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "위시리스트에서 해당 항목을 찾을 수 없습니다."),
    PRODUCT_ALREADY_IN_WISHLIST(HttpStatus.CONFLICT, "해당 상품이 이미 위시리스트에 존재합니다."),

    // 재고 관련 에러
    STOCK_NOT_ENOUGH(HttpStatus.BAD_REQUEST, "상품의 재고가 부족합니다."),
    INVALID_QUANTITY(HttpStatus.BAD_REQUEST, "수량은 0보다 커야 합니다."),

    //주문 관련 에러
    EVENT_NOT_FOUND(HttpStatus.NOT_FOUND, "이벤트 정보를 찾을 수 없습니다."),
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "주문 정보를 찾을 수 없습니다."),
    ORDER_CANCELLATION_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "주문 상태가 배송중이 되기 이전까지만 취소가 가능합니다."),
    RETURN_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "배송 완료 후 D+1일까지만 반품이 가능합니다."),
    INVALID_ORDER_STATUS(HttpStatus.BAD_REQUEST, "유효하지 않은 주문 상태입니다."),
    INVALID_ORDER_STATUS_DESCRIPTION(HttpStatus.BAD_REQUEST, "유효하지 않은 주문 상태 DESCRIPTION 입니다."),
    INVALID_TOTAL_PRICE(HttpStatus.BAD_REQUEST, "총 금액은 0보다 작을 수 없습니다."),
    ORDER_CANCELLATION_PERIOD_EXPIRED(HttpStatus.BAD_REQUEST, "취소는 주문 상태가 배송중이 되기 이전까지만 가능합니다."),
    RETURN_NOT_ALLOWED_NOT_DELIVERED(HttpStatus.BAD_REQUEST, "반품은 배송 완료 상태이거나 배송 완료 후 D+1일까지만 반품이 가능합니다."),
    RETURN_PERIOD_EXPIRED(HttpStatus.BAD_REQUEST, "반품 요청 가능 기간이 지났습니다."),
    RETURN_NOT_ALLOWED_NOT_REQUESTED(HttpStatus.BAD_REQUEST, "반품 요청 상태에서만 반품 완료가 가능합니다."),
    ORDER_CANNOT_UPDATE_CANCELLED(HttpStatus.BAD_REQUEST, "취소된 주문은 상태를 변경할 수 없습니다."),
    EVENT_NOT_LINKED_TO_PRODUCT(HttpStatus.BAD_REQUEST, "해당 상품에 대한 이벤트가 존재하지 않습니다."),
    EVENT_SERVICE_EXPIRED(HttpStatus.BAD_REQUEST, "이벤트의 서비스 기간이 지났습니다."),
    STOCK_DECREASE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "재고 감소 처리에 실패했습니다."),
    PAYMENT_FAILURE(HttpStatus.BAD_REQUEST, "결제 처리에 실패했습니다."),
    PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "결제 정보가 존재하지 않습니다."),

    // 이베느 관련
    EVENT_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "이벤트가 이미 존재합니다."), // 이벤트가 이미 존재할 경우
    INVALID_DATE_RANGE(HttpStatus.BAD_REQUEST, "시작일자가 종료일자보다 늦을 수 없습니다."), // 날짜 범위가 잘못된 경우
    ;


    private final HttpStatus httpStatus;
    private final String message;

    public int getStatusCode() {
        return httpStatus.value();
    }
}
