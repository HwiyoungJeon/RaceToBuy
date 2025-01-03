package com.jh.orderservice.domain.order.dto;

import com.jh.orderservice.domain.order.entity.Order;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
public class OrderResponseDTO {
    private Long orderId;             // 주문 ID
    private BigDecimal totalPrice;    // 총 주문 금액
    private String orderStatus;         // 주문 상태
    private List<OrderDetailDTO> orderDetails; // 주문 상세 리스트

    public static OrderResponseDTO fromEntity(Order order) {
        // OrderDetailDTO 리스트로 변환
        List<OrderDetailDTO> details = order.getOrderDetails().stream()
                .map(OrderDetailDTO::fromEntity)
                .collect(Collectors.toList());

        return new OrderResponseDTO(
                order.getOrderId(),
                order.getTotalPrice(),
                order.getOrderStatus().getDescription(),
                details
        );
    }
}
