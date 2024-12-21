package com.example.racetobuy.domain.product.dto;


import java.math.BigDecimal;
import java.util.List;

public record ProductWithEventDTO(
        Long productId,          // 상품 ID
        String productName,      // 상품 이름
        BigDecimal price,        // 상품 가격
        Integer stockQuantity,   // 재고 수량
        List<EventInfoDTO> events        // 활성 이벤트 정보 (null 가능)
) {}