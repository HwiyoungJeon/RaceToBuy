package com.jh.productservice.domain.product.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class EventDateUpdateRequest {

    private Long id;               // 이벤트 ID

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")  // 입력 값 포맷
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startDate;    // 시작일자

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")  // 입력 값 포맷
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endDate;      // 종료일자

}

