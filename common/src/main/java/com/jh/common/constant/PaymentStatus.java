package com.jh.common.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PaymentStatus {

    PROCESSING("결제 중"),  // 결제 요청 후, PG사로 요청을 보낸 상태
    COMPLETED("결제 완료"),  // 결제가 성공적으로 완료된 상태
    FAILED("결제 실패"),     // 결제 실패 상태
    CANCELLED("결제 취소");
    // 결제 취소 상태
    private final String description;

    public String getDescription() {
        return description;
    }
}
