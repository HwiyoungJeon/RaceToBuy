package com.jh.orderservice.domain.payment.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jh.common.constant.PaymentFailureReason;
import com.jh.common.constant.PaymentStatus;
import com.jh.common.domain.timestamp.TimeStamp;
import com.jh.orderservice.domain.order.entity.Order;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "payment")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment extends TimeStamp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Long paymentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "payment_method", nullable = false)
    private String paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    private PaymentStatus paymentStatus;

    @Column(name = "failure_reason")
    private PaymentFailureReason failureReason;

    @Builder
    public Payment(Order order, String paymentMethod, PaymentStatus paymentStatus, PaymentFailureReason failureReason) {
        this.order = order;
        this.paymentMethod = paymentMethod;
        this.paymentStatus = paymentStatus;
        this.failureReason = failureReason;
    }

    public Payment withStatus(PaymentStatus paymentStatus) {
        return new Payment(this.order, this.paymentMethod, paymentStatus, this.failureReason);
    }

    public Payment withFailureReason(PaymentFailureReason failureReason) {
        return new Payment(this.order, this.paymentMethod, this.paymentStatus, failureReason);
    }

    // 새로운 updateStatus 메서드 추가
    public void updateStatus(PaymentStatus newStatus) {
        // 상태가 이미 새로운 상태와 같다면 변경하지 않음
        if (this.paymentStatus != newStatus) {
            this.paymentStatus = newStatus;
        }
    }
}

