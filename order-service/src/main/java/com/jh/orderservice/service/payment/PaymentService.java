package com.jh.orderservice.service.payment;

import com.jh.common.util.ApiResponse;
import com.jh.orderservice.domain.payment.dto.PaymentResponseDto;

public interface PaymentService {
    // 결제 처리
    PaymentResponseDto processPayment(Long orderId, String paymentMethod);

    ApiResponse<?> getAllPaymentsForOrder(Long orderId);
}
