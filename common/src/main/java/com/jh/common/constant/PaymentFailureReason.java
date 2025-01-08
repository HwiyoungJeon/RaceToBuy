package com.jh.common.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PaymentFailureReason {
    INSUFFICIENT_FUNDS("잔액 부족"),
    NETWORK_ERROR("네트워크 오류"),
    USER_CANCELLED("사용자 취소"),
    PAYMENT_LIMIT_EXCEEDED("카드 한도 초과"),
    CARD_EXPIRED("카드 만료"),
    SERVICE_UNAVAILABLE("서비스 이용 불가"),
    OTHER("기타");

    private final String description;

    public String getDescription() {
        return description;
    }

    // 랜덤으로 실패 사유를 반환하는 메서드
    public static PaymentFailureReason getRandomFailureReason() {
        PaymentFailureReason[] values = values();
        int randomIndex = (int) (Math.random() * values.length); // 0부터 values.length-1까지의 랜덤 인덱스
        return values[randomIndex];
    }

}
