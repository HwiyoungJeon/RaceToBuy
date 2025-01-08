package com.jh.orderservice.service.payment;

import com.jh.common.util.ApiResponse;

public interface PaymentService {
    // 결제 처리
    ApiResponse<?> processPayment(Long orderId, String paymentMethod);

    ApiResponse<?> getAllPaymentsForOrder(Long orderId);
}
