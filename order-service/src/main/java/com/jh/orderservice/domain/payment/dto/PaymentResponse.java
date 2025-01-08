package com.jh.orderservice.domain.payment.dto;

import com.jh.orderservice.domain.order.entity.Order;
import com.jh.orderservice.domain.order.entity.OrderDetail;
import com.jh.orderservice.domain.payment.entity.Payment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
@AllArgsConstructor
public class PaymentResponse {

    private Long paymentId;
    private Long orderId;
    private String paymentMethod;
    private String paymentStatus;
    private String failureReason; // 실패 사유가 있을 경우
    private OrderInfo orderInfo;  // 주문 정보 추가

    public static PaymentResponse from(Payment payment) {
        Order order = payment.getOrder();
        String failureReason = (payment.getFailureReason() != null && payment.getFailureReason().getDescription() != null)
                ? payment.getFailureReason().getDescription()
                : null;


        return PaymentResponse.builder()
                .paymentId(payment.getPaymentId())
                .orderId(payment.getOrder().getOrderId())
                .paymentMethod(payment.getPaymentMethod())
                .paymentStatus(payment.getPaymentStatus().getDescription())
                .failureReason(failureReason)
                .orderInfo(new OrderInfo(order))
                .build();
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class OrderInfo {
        private Long orderId;
        private BigDecimal totalPrice;    // 총 금액
        private String orderStatus;   // 주문 상태
        private List<OrderDetailInfo> orderDetails;  // 주문 항목 정보 리스트

        // Order 객체에서 OrderInfo로 변환
        public OrderInfo(Order order) {
            this.orderId = order.getOrderId();
            this.totalPrice = order.getTotalPrice();  // 총 금액
            this.orderStatus = order.getOrderStatus() != null ? order.getOrderStatus().getDescription() : null;  // 주문 상태
            // 주문 항목 정보
            this.orderDetails = order.getOrderDetails() != null
                    ? order.getOrderDetails().stream()
                    .map(OrderDetailInfo::new)
                    .collect(Collectors.toList())
                    : null;
        }
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class OrderDetailInfo {
        private Long productId;
        private String productName;
        private int quantity;
        private BigDecimal price;
        private Long eventId;
        private String eventName;
        private BigDecimal discountPrice;

        // 주문 항목 정보 세팅
        public OrderDetailInfo(OrderDetail orderDetail) {
            this.productId = orderDetail.getProductId();
            this.productName = orderDetail.getProductName();
            this.quantity = orderDetail.getQuantity();
            this.price = orderDetail.getPrice();
            this.eventId = orderDetail.getEventProductId();
            this.eventName = orderDetail.getEventProductName();
            this.discountPrice = orderDetail.getDiscountPrice();
        }
    }
}