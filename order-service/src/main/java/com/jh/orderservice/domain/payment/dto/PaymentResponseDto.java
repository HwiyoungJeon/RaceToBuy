package com.jh.orderservice.domain.payment.dto;

import com.jh.common.constant.PaymentStatus;
import lombok.Getter;

@Getter
public class PaymentResponseDto {

    private final Long orderId;
    private final String paymentStatus;
    private final String failureReason;

    // Constructor
    public PaymentResponseDto(Long orderId, PaymentStatus paymentStatus, String failureReason) {
        if (orderId == null || paymentStatus == null) {
            throw new IllegalArgumentException("필수 필드는 null일 수 없습니다.");
        }
        this.orderId = orderId;
        this.paymentStatus = paymentStatus.getDescription();
        this.failureReason = failureReason;
    }

    // toString() for logging or debugging purposes
    @Override
    public String toString() {
        return "PaymentResponseDto{" +
                "orderId=" + orderId +
                ", paymentStatus=" + paymentStatus +
                ", failureReason='" + failureReason + '\'' +
                '}';
    }
}
