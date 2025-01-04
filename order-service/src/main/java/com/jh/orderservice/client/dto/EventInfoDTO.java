package com.jh.orderservice.client.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class EventInfoDTO {

    private Long eventId;
    private String eventName;
    private Double discountRate;
    private BigDecimal discountPrice;
    private BigDecimal priceDifference;

}