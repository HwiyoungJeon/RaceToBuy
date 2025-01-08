package com.jh.orderservice.domain.order.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jh.common.constant.ErrorCode;
import com.jh.common.constant.OrderStatus;
import com.jh.common.domain.timestamp.TimeStamp;
import com.jh.common.exception.BusinessException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Getter
@Entity
@Table(name = "orders")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends TimeStamp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "member_id")
    private Long memberId;

    @Column(name = "total_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;

    @Column(name = "order_status", nullable = false)
    private OrderStatus orderStatus;

    @Column(name = "delivered_date")
    private LocalDateTime deliveredDate;

    @Column(name = "day_offset")
    private int dayOffset;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<OrderDetail> orderDetails = new ArrayList<>();


    @Builder
    public Order(Long memberId, BigDecimal totalPrice, OrderStatus orderStatus, LocalDateTime deliveredDate, int dayOffset) {
        this.memberId = memberId;
        this.totalPrice = totalPrice;
        this.orderStatus = orderStatus;
        this.deliveredDate = deliveredDate;
        this.dayOffset = dayOffset;
    }

    /**
     * 총 주문 금액 업데이트
     *
     * @param totalPrice 업데이트할 총 금액
     */
    public void updateTotalPrice(BigDecimal totalPrice) {
        if (totalPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException(ErrorCode.INVALID_TOTAL_PRICE);
        }
        this.totalPrice = totalPrice;
    }

    /**
     * 주문 상태를 '취소됨'으로 변경
     */
    public void cancelOrder() {
        if (!this.orderStatus.equals(OrderStatus.ORDERED)) {
            throw new BusinessException(ErrorCode.ORDER_CANCELLATION_PERIOD_EXPIRED);
        }
        this.orderStatus = OrderStatus.CANCELLED;
    }

    /**
     * 배송 완료 처리 (D+0 시점)
     */
    public void markAsDelivered() {
        this.orderStatus = OrderStatus.DELIVERED;
        this.deliveredDate = LocalDateTime.now();
    }

    /**
     * 반품 요청
     */
    public void requestReturn() {
        if (!OrderStatus.DELIVERED.equals(this.orderStatus) && !OrderStatus.DELIVERED_DAY1.equals(this.orderStatus)) {
            throw new BusinessException(ErrorCode.RETURN_NOT_ALLOWED_NOT_DELIVERED);
        }

        if (this.deliveredDate.plusDays(1).isBefore(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.RETURN_PERIOD_EXPIRED);
        }

        this.orderStatus = OrderStatus.RETURN_REQUESTED;
    }

    /**
     * 반품 처리 완료 (D+1 시점)
     */
    public void completeReturn() {
        if (!OrderStatus.RETURN_REQUESTED.equals(this.orderStatus)) {
            throw new BusinessException(ErrorCode.RETURN_NOT_ALLOWED_NOT_REQUESTED);
        }

        this.orderStatus = OrderStatus.RETURNED;
    }

    /**
     * 주문 상세 추가
     *
     * @param orderDetail 추가할 주문 상세
     */
    public void addOrderDetail(OrderDetail orderDetail) {
        this.orderDetails.add(orderDetail);
        orderDetail.assignOrder(this);
    }

    /**
     * 주문 상태 동적 업데이트
     *
     * @param newStatus 새로운 주문 상태
     */
    public void updateOrderStatus(OrderStatus newStatus) {
        System.out.println("Before updating status, current status: " + this.orderStatus);
        if (newStatus == null) {
            throw new BusinessException(ErrorCode.INVALID_ORDER_STATUS);
        }
        // 현재 상태 확인 로그

        // 예외 조건을 추가하고 싶다면 여기서 처리 가능
        if (this.orderStatus.equals(OrderStatus.CANCELLED)) {
            throw new BusinessException(ErrorCode.ORDER_CANNOT_UPDATE_CANCELLED);
        }

        this.orderStatus = newStatus;

        System.out.println("After updating status, new status: " + this.orderStatus);
        // 배송 완료 상태일 경우, 배송 완료 시간 기록
        if (newStatus.equals(OrderStatus.DELIVERED)) {
            this.markAsDelivered();
        }
    }

    /**
     * dayOffset 값을 증가시키는 메서드
     *
     * @param dayOffset 추가할 값
     */
    public void updateSetDayOffset(int dayOffset) {
        this.dayOffset = dayOffset;
    }
}