package com.example.racetobuy.domain.order.dto;

import lombok.Getter;

@Getter
public class OrderRequestDTO {
    private Long productId;    // 상품 ID
    private Integer quantity;  // 수량
    private Long eventId;      // 적용 이벤트 ID (선택 사항)
}
