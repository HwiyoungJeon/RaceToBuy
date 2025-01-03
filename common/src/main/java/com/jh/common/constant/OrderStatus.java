package com.jh.common.constant;


import com.jh.common.exception.BusinessException;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum OrderStatus {
    ORDERED("주문 완료"),
    SHIPPING("배송 중"),
    DELIVERED("배송 완료"),
    RETURN_REQUESTED("반품 요청됨"),
    RETURNED("반품 완료"),
    CANCELLED("취소 완료"),
    DELIVERED_DAY1("배송 완료 D+1"),
    DELIVERED_NOT_FOUNT("이벤트의 서비스 기간이 지났습니다.");

    private final String description;

    /**
     * 상태 이름으로 OrderStatus를 찾는 메서드
     *
     * @param name 상태 이름
     * @return OrderStatus
     * @throws BusinessException 상태 이름이 유효하지 않을 경우
     */
    public static OrderStatus findByName(String name) {
        return Arrays.stream(OrderStatus.values())
                .filter(status -> status.name().equalsIgnoreCase(name.trim()))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_ORDER_STATUS));
    }

    /**
     * 상태 설명으로 OrderStatus를 찾는 메서드
     *
     * @param description 상태 설명
     * @return OrderStatus
     * @throws BusinessException 상태 설명이 유효하지 않을 경우
     */
    public static OrderStatus findByDescription(String description) {
        return Arrays.stream(OrderStatus.values())
                .filter(status -> status.getDescription().equalsIgnoreCase(description.trim()))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_ORDER_STATUS_DESCRIPTION));
    }
}