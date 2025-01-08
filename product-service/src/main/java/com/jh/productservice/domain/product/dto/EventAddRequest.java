package com.jh.productservice.domain.product.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Getter
public class EventAddRequest {
    private String eventName;       // 이벤트 이름
    private Double discountRate;    // 할인율

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")  // 입력 값 포맷
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startDate;    // 시작일자

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")  // 입력 값 포맷
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endDate;      // 종료일자

    @JsonCreator
    public EventAddRequest(String eventName, Double discountRate, LocalDateTime startDate, LocalDateTime endDate) {
        this.eventName = eventName;
        this.discountRate = discountRate;
        this.startDate = startDate;
        this.endDate = endDate;
    }
}
