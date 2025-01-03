package com.jh.productservice.domain.product.dto;


import java.math.BigDecimal;

public record EventInfoDTO(
        Long eventId,             // 이벤트 ID
        String eventName,         // 이벤트 이름
        Double discountRate,      // 할인율 (%)
        BigDecimal discountPrice, // 할인가
        BigDecimal priceDifference // 가격 차이 (원가 - 할인가)
) {
}