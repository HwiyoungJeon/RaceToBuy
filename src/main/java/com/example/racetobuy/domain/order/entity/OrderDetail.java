package com.example.racetobuy.domain.order.entity;

import com.example.racetobuy.domain.product.entity.EventProduct;
import com.example.racetobuy.domain.product.entity.Product;
import com.example.racetobuy.domain.timestamp.TimeStamp;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    private EventProduct eventProduct; // 사용된 이벤트 정보

    @Column(name = "discount_price", nullable = false)
    private BigDecimal discountPrice; // 할인 적용 후 금액

    @Builder
    public OrderDetail(Order order,Product product, Integer quantity, BigDecimal price,EventProduct eventProduct,BigDecimal discountPrice) {
        this.order = order;
        this.product = product;
        this.quantity = quantity;
        this.price = price;
        this.eventProduct = eventProduct;
        this.discountPrice = discountPrice;
    }

    public void assignOrder(Order order) {
        this.order = order;
    }
}