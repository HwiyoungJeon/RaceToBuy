package com.jh.productservice.domain.product.entity;


import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.jh.common.domain.timestamp.TimeStamp;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Table(name = "event")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Event extends TimeStamp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_id")
    private Long eventId;

    @Column(name = "event_name", nullable = false, length = 100)
    private String eventName; // 이벤트 이름

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate; // 시작일

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate; // 종료일

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<EventProduct> eventProducts = new ArrayList<>();

    public Event(String eventName, LocalDateTime startDate, LocalDateTime endDate) {
        this.eventName = eventName;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public void updateDates(LocalDateTime startDate, LocalDateTime endDate) {
        // 시작일자가 종료일자보다 클 수 없도록 예외 처리
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("시작일자가 종료일자보다 클 수 없습니다.");
        }
        this.startDate = startDate;
        this.endDate = endDate;
    }
}