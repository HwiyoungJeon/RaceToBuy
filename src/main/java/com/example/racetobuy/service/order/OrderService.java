package com.example.racetobuy.service.order;

import com.example.racetobuy.domain.order.dto.DayOffsetRequest;
import com.example.racetobuy.domain.order.dto.OrderRequestDTO;
import com.example.racetobuy.global.util.ApiResponse;

import java.util.List;

public interface OrderService {

    ApiResponse<?> createOrder(Long memberId, List<OrderRequestDTO> orderRequest);

    ApiResponse<?> cancelOrder(Long memberId, Long orderId);

    ApiResponse<?> returnOrder(Long memberId, Long orderId);

//    ApiResponse<?> markOrderAsDelivered(Long orderId);

    /**
     * 주문 상태 변경 (dayOffset 사용)
     *
     * @param orderId 주문 ID
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
}
