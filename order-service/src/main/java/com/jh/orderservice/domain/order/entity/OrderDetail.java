package com.jh.orderservice.domain.order.entity;

import com.jh.common.domain.timestamp.TimeStamp;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Entity
@Table(name = "order_detail")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderDetail extends TimeStamp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_detail_id")
    private Long orderDetailId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

//

    @Column(name = "product_id", nullable = false)
    private Long productId; // Foreign Key로 변경

    @Column(name = "product_name", nullable = false)
    private String productName; // Foreign Key로 변경

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "event_product_id")
    private Long eventProductId; // Foreign Key

    @Column(name = "event_product_name")
    private String eventProductName; // Foreign Key

    @Column(name = "discount_price", nullable = false)
    private BigDecimal discountPrice; // 할인 적용 후 금액

    @Builder
    public OrderDetail(Order order, Long productId, String productName, Integer quantity, BigDecimal price, Long eventProductId, String eventProductName, BigDecimal discountPrice) {
        this.order = order;
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.price = price;
        this.eventProductId = eventProductId;
        this.eventProductName = eventProductName;
        this.discountPrice = discountPrice;
    }

    public void assignOrder(Order order) {
        this.order = order;
    }

}