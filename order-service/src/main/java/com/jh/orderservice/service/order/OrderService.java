package com.jh.orderservice.service.order;

import com.jh.common.util.ApiResponse;
import com.jh.orderservice.domain.order.dto.DayOffsetRequest;
import com.jh.orderservice.domain.order.dto.OrderRequestDTO;

import java.util.List;

public interface OrderService {

    ApiResponse<?> createPendingOrder(Long memberId, List<OrderRequestDTO> orderRequest);

    ApiResponse<?> completeOrder(Long memberId, Long orderId, List<OrderRequestDTO> orderRequest);

    ApiResponse<?> cancelOrder(Long memberId, Long orderId);

    ApiResponse<?> returnOrder(Long memberId, Long orderId);

//    ApiResponse<?> markOrderAsDelivered(Long orderId);

    /**
     * 주문 상태 변경 (dayOffset 사용)
     *
     * @param orderId          주문 ID
     * @param dayOffsetRequest 변경할 dayOffset 값
     * @return ApiResponse
     */
    ApiResponse<?> updateOrderStatus(Long orderId, DayOffsetRequest dayOffsetRequest);

    /**
     * 회원의 모든 주문 조회
     *
     * @param memberId 회원 ID
     * @return ApiResponse
     */
    ApiResponse<?> getOrdersByMemberId(Long memberId, Long page, int size);

    /**
     * 특정 주문 상세 조회
     *
     * @param orderId  주문 ID
     * @param memberId 회원 ID
     * @return ApiResponse
     */
    ApiResponse<?> getOrderDetailsById(Long orderId, Long memberId);

    ApiResponse<?> processPayment(Long orderId, String paymentMethod);
}
