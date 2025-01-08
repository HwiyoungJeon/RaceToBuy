package com.jh.productservice.domain.product.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class EventResponseDTO {
    private Long eventId;
    private String eventName;
    private Double discountRate;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")  // 입력 값 포맷
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startDate;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")  // 입력 값 포맷
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endDate;
}
