package com.jh.orderservice.controller;

import com.jh.common.util.ApiResponse;
import com.jh.orderservice.domain.order.dto.DayOffsetRequest;
import com.jh.orderservice.domain.order.dto.OrderRequestDTO;
import com.jh.orderservice.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
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
            HttpServletRequest request,
            @RequestParam(required = false, defaultValue = "0") Long cursor,
            @RequestParam(defaultValue = "10") int size) {
        // 헤더에서 id와 role 값을 읽어옴
        String id = request.getHeader("X-Authorization-Id");
//        String role = request.getHeader("X-Authorization-Role");
        Long memberId = null;
        try {
            assert id != null;
            memberId = Long.parseLong(id); // id 값을 Long으로 변환
        } catch (NumberFormatException e) {
            System.out.println("에러 : ㅠ");
        }

        System.out.println("memberId: " + memberId); // 디버깅을 위한 로그

        // memberId를 사용하여 주문 조회 서비스 호출
        ApiResponse<?> response = orderService.getOrdersByMemberId(memberId, cursor, size);

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
        Long memberId = (Long) auth.getPrincipal();
        ApiResponse<?> response = orderService.getOrderDetailsById(orderId, memberId);
        return ResponseEntity.ok(response);
    }

    /**
     * 주문 생성
     *
     * @param orderRequest 주문할 상품 및 수량 정보 리스트
     * @return ApiResponse  주문 생성 결과
     */
    @PostMapping
    public ApiResponse<?> createOrder(
            @RequestBody List<OrderRequestDTO> orderRequest) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Long memberId = (Long) auth.getPrincipal();
            // 디버깅을 위한 로그 추가
            System.out.println("회원 ID: " + memberId);
            System.out.println("주문 요청 데이터: " + orderRequest);

            ApiResponse<?> response = orderService.createOrder(memberId, orderRequest);

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
        Long memberId = (Long) auth.getPrincipal();
        return orderService.cancelOrder(memberId, orderId);
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
        Long memberId = (Long) auth.getPrincipal();
        return orderService.returnOrder(memberId, orderId);
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