package com.example.racetobuy.controller;

import com.example.racetobuy.domain.order.dto.DayOffsetRequest;
import com.example.racetobuy.domain.order.dto.OrderRequestDTO;
import com.example.racetobuy.global.security.MemberDetails;
import com.example.racetobuy.global.util.ApiResponse;
import com.example.racetobuy.service.order.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    /**
     * 주문 조회
     *
     * @param memberDetails 사용자 인증 정보
     * @return ApiResponse
     */
    @GetMapping
    public ResponseEntity<ApiResponse<?>> getOrders(@AuthenticationPrincipal MemberDetails memberDetails,
                                                    @RequestParam(required = false, defaultValue = "0") Long cursor,
                                                    @RequestParam(defaultValue = "10") int size) {
        ApiResponse<?> response = orderService.getOrdersByMemberId(memberDetails.getMemberId(),cursor,size);
        return ResponseEntity.ok(response);
    }

    /**
     * 특정 주문 상세 조회
     *
     * @param orderId       주문 ID
     * @param memberDetails 사용자 인증 정보
     * @return ApiResponse
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<?>> getOrderDetails(
            @PathVariable Long orderId,
            @AuthenticationPrincipal MemberDetails memberDetails) {
        ApiResponse<?> response = orderService.getOrderDetailsById(orderId, memberDetails.getMemberId());
        return ResponseEntity.ok(response);
    }

    /**
     * 주문 생성
     *
     * @param memberDetails 현재 로그인한 사용자의 정보
     * @param orderRequest  주문할 상품 및 수량 정보 리스트
     * @return ApiResponse  주문 생성 결과
     */
    @PostMapping
    public ApiResponse<?> createOrder(@AuthenticationPrincipal MemberDetails memberDetails,
                                      @RequestBody List<OrderRequestDTO> orderRequest) {
        return orderService.createOrder(memberDetails.getMemberId(), orderRequest);
    }

    /**
     * 주문 취소
     *
     * @param memberDetails 현재 로그인한 사용자의 정보
     * @param orderId       취소할 주문 ID
     * @return ApiResponse  주문 취소 결과
     */
    @PostMapping("/{orderId}/cancel")
    public ApiResponse<?> cancelOrder(@AuthenticationPrincipal MemberDetails memberDetails,
                                      @PathVariable Long orderId) {
        return orderService.cancelOrder(memberDetails.getMemberId(), orderId);
    }

    /**
     * 주문 반품 요청
     *
     * @param memberDetails 현재 로그인한 사용자의 정보
     * @param orderId       반품할 주문 ID
     * @return ApiResponse  반품 요청 결과
     */
    @PostMapping("/{orderId}/return")
    public ApiResponse<?> returnOrder(@AuthenticationPrincipal MemberDetails memberDetails,
                                      @PathVariable Long orderId) {
        return orderService.returnOrder(memberDetails.getMemberId(), orderId);
    }

    /**
     * 배송 완료 처리
     *
     * @param orderId 주문 ID
     * @return ApiResponse
     */
//    @PatchMapping("/{orderId}/deliver")
//    public ResponseEntity<ApiResponse<?>> markAsDelivered(@PathVariable Long orderId) {
//        ApiResponse<?> response = orderService.markOrderAsDelivered(orderId);
//        return ResponseEntity.ok(response);
//    }


    /**
     * 주문 상태 변경
     *
     * @param orderId 주문 ID
     * @param dayOffsetRequest  변경할 주문 상태
     * @return ApiResponse
     */
    @PatchMapping("/{orderId}/dayOffset")
    public ResponseEntity<ApiResponse<?>> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestBody DayOffsetRequest dayOffsetRequest) {
        ApiResponse<?> response = orderService.updateOrderStatus(orderId, dayOffsetRequest);
        return ResponseEntity.ok(response);
    }
}