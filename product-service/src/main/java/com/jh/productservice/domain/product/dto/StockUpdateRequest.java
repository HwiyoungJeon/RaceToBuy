package com.jh.productservice.domain.product.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class StockUpdateRequest {
    private Long productId;
    private Integer quantity;
} 