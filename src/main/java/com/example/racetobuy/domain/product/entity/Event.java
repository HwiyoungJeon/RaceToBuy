package com.example.racetobuy.domain.product.entity;

import com.example.racetobuy.domain.timestamp.TimeStamp;
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
    private List<EventProduct> eventProducts = new ArrayList<>();
}