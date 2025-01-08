package com.jh.orderservice.controller;

import com.jh.common.util.ApiResponse;
import com.jh.orderservice.domain.payment.dto.PaymentDto;
import com.jh.orderservice.service.payment.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/payments")
public class PaymentController {
    private final PaymentService paymentService;

    /**
     * 결제 처리
     * @param orderId 주문 ID
     * @param paymentDto 결제 방식 (예: 카드, 계좌이체 등)
     * @return 결제 처리 결과
     */
    @PostMapping("/{orderId}/payment")
    public ResponseEntity<ApiResponse<?>> processPayment(
            @PathVariable Long orderId,
            @RequestBody PaymentDto paymentDto) {
        // 결제 처리
        ApiResponse<?> response = paymentService.processPayment(orderId, paymentDto.getPaymentMethod());
        return ResponseEntity.ok(response);
    }

    /**
     * 결제 상태 조회
     * @param orderId 주문 ID
     * @return 결제 정보
     */
    @GetMapping("/{orderId}/status")
    public ResponseEntity<ApiResponse<?>> getPaymentStatus(
            @PathVariable Long orderId) {

        ApiResponse<?> response = paymentService.getAllPaymentsForOrder(orderId);
        return ResponseEntity.ok(response);
    }
}
