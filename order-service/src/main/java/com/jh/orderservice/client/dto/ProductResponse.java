package com.jh.orderservice.client.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ProductResponse {
    private Long productId;
    private String productName;
    private BigDecimal price;
    private Integer stockQuantity;
    private List<EventInfoDTO> events;
} 