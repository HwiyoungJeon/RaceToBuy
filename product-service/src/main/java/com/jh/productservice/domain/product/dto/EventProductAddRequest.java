package com.jh.productservice.domain.product.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
public class EventProductAddRequest {
    private Long productId;
    private Long eventId;
    private Double discountRate;

    @JsonCreator
    public EventProductAddRequest(Long productId, Long eventId, Double discountRate) {
        this.productId = productId;
        this.eventId = eventId;
        this.discountRate = discountRate;
    }
}