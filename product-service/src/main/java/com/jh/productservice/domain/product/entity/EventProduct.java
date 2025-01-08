package com.jh.productservice.domain.product.entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jh.common.domain.timestamp.TimeStamp;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "event_product")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EventProduct extends TimeStamp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    @JsonIgnore
    private Event event; // 이벤트

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @JsonIgnore
    private Product product; // 상품

    @Column(name = "discount_rate", nullable = false, columnDefinition = "DECIMAL(5,2)")
    private Double discountRate; // 상품에 대한 할인율

    public EventProduct(Event event, Product product, Double discountRate) {
        this.event = event;
        this.product = product;
        this.discountRate = discountRate;
    }
}
