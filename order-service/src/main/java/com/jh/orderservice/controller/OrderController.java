package com.jh.orderservice.controller;

import com.jh.common.util.ApiResponse;
import com.jh.orderservice.domain.order.dto.DayOffsetRequest;
import com.jh.orderservice.domain.order.dto.OrderRequestDTO;
import com.jh.orderservice.service.OrderService;
import com.jh.userservice.security.MemberDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
     * @param memberId 사용자 인증 정보
     * @return ApiResponse
     */
    @GetMapping
    public ResponseEntity<ApiResponse<?>> getOrders(
//        @RequestHeader("Authorization-Id") Long memberId,
            @RequestParam(required = false, defaultValue = "0") Long cursor,
            @RequestParam(defaultValue = "10") int size) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        MemberDetails memberDetails = (MemberDetails) auth.getPrincipal();

        ApiResponse<?> response = orderService.getOrdersByMemberId(memberDetails.getMemberId(), cursor, size);
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
            @PathVariable Long orderId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        MemberDetails memberDetails = (MemberDetails) auth.getPrincipal();
        ApiResponse<?> response = orderService.getOrderDetailsById(orderId, memberDetails.getMemberId());
        return ResponseEntity.ok(response);
    }

    /**
     * 주문 생성
     *
     * @param orderRequest 주문할 상품 및 수량 정보 리스트
     * @return ApiResponse  주문 생성 결과
     */
    @PostMapping
    public ApiResponse<?> createOrder(@RequestBody List<OrderRequestDTO> orderRequest) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            MemberDetails memberDetails = (MemberDetails) auth.getPrincipal();

            // 디버깅을 위한 로그 추가
            System.out.println("회원 ID: " + memberDetails.getMemberId());
            System.out.println("주문 요청 데이터: " + orderRequest);

            ApiResponse<?> response = orderService.createOrder(memberDetails.getMemberId(), orderRequest);

            // 디버깅을 위한 반환 값 확인
            System.out.println("주문 생성 응답: " + response);

            return response;
        } catch (Exception e) {
            // 예외 발생 시 처리
            e.printStackTrace();
            return ApiResponse.createException(404, "주문 오류발생");
        }
    }

    /**
     * 주문 취소
     *
     * @param memberDetails 현재 로그인한 사용자의 정보
     * @param orderId       취소할 주문 ID
     * @return ApiResponse  주문 취소 결과
     */
    @PostMapping("/{orderId}/cancel")
    public ApiResponse<?> cancelOrder(@PathVariable Long orderId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        MemberDetails memberDetails = (MemberDetails) auth.getPrincipal();
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
    public ApiResponse<?> returnOrder(@PathVariable Long orderId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        MemberDetails memberDetails = (MemberDetails) auth.getPrincipal();
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
     * @param orderId          주문 ID
     * @param dayOffsetRequest 변경할 주문 상태
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