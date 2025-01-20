package com.jh.orderservice.aop;

import com.jh.common.constant.ErrorCode;
import com.jh.common.constant.OrderStatus;
import com.jh.common.constant.PaymentStatus;
import com.jh.common.exception.BusinessException;
import com.jh.common.util.ApiResponse;
import com.jh.orderservice.client.ProductServiceClient;
import com.jh.orderservice.client.dto.StockUpdateRequest;
import com.jh.orderservice.domain.order.entity.Order;
import com.jh.orderservice.domain.order.repository.OrderRepository;
import com.jh.orderservice.domain.payment.dto.PaymentResponseDto;
import com.jh.orderservice.domain.payment.entity.Payment;
import com.jh.orderservice.domain.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class OrderStatusAspect {

    private final OrderRepository orderRepository;
    private final ProductServiceClient productClient;
    private final PaymentRepository paymentRepository;

    @AfterReturning(pointcut = "execution(* com.jh.orderservice.service.order.OrderServiceImpl.updateOrderStatus(..)) || execution(* com.jh.orderservice.service.order.OrderServiceImpl.returnOrder(..))", returning = "result")
    public void updateOrderStatusAfterProcessing(Object result) {
        // 결과에서 Order ID 추출
        Long updatedOrderId = extractOrderIdFromResult(result);
        if (updatedOrderId == null) {
            System.out.println("No order ID found in the result. Skipping.");
            return;
        }

        // 특정 Order ID로 주문 조회
        Order order = orderRepository.findById(updatedOrderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
        // 반품 처리
        if (OrderStatus.RETURNED.equals(order.getOrderStatus())) {
            restoreStockForReturnedOrder(order);
        }


        // 로직: DayOffset과 상태 업데이트
        if (order.getDayOffset() < 2) {
            order.updateSetDayOffset(order.getDayOffset() + 1);

            if (order.getDayOffset() == 1) {
                order.updateOrderStatus(OrderStatus.SHIPPING); // 배송중
            } else if (order.getDayOffset() == 2) {
                order.updateOrderStatus(OrderStatus.DELIVERED); // 배송 완료
            }

            orderRepository.save(order); // 상태 업데이트
        }

        System.out.println("Order ID " + updatedOrderId + " successfully processed.");
    }
    private Long extractOrderIdFromResult(Object result) {
        // result가 PaymentResponseDto인 경우
        if (result instanceof PaymentResponseDto paymentResponse) {

            // orderId 반환
            return paymentResponse.getOrderId();
        }

        // result가 PaymentResponseDto가 아닌 경우 예외 처리
        throw new BusinessException(ErrorCode.INVALID_RESULT_TYPE);
    }


    /**
     * 반품 완료 시 재고 복구 로직
     */
    private void restoreStockForReturnedOrder(Order order) {
        order.getOrderDetails().forEach(detail -> {
            productClient.increaseStock(new StockUpdateRequest(
                detail.getProductId(),
                detail.getQuantity()
            ));
        });
    }

    /**
     * 재고 복구 및 결제 취소
     */
    private void restoreStockForFailedOrder(Order order) {
        // 재고 복구
        order.getOrderDetails().forEach(detail -> {
            ApiResponse<Boolean> restoreResponse = productClient.increaseStock(
                    StockUpdateRequest.builder()
                            .productId(detail.getProductId())
                            .quantity(detail.getQuantity())
                            .build()
            );

            if (restoreResponse == null || !Boolean.TRUE.equals(restoreResponse.getData())) {
                log.error("재고 복구 실패: 제품 ID {}", detail.getProductId());
            } else {
                log.info("제품 ID {}에 대해 재고 복구 완료", detail.getProductId());
            }
        });

        // 결제 취소 (예: 결제 상태 변경)
        Payment payment = paymentRepository.findByOrder(order)
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));

        payment.updateStatus(PaymentStatus.CANCELLED); // 결제 취소 상태로 변경
        paymentRepository.save(payment);

        log.info("결제 취소 완료: 주문 ID {}", order.getOrderId());
    }

}