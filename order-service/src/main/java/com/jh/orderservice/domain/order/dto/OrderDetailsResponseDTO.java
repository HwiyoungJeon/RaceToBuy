package com.jh.orderservice.domain.order.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.jh.orderservice.domain.order.entity.Order;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class OrderDetailsResponseDTO {
    private Long orderId;
    private String orderStatus;
    private BigDecimal totalPrice;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime deliveredDate;
    private List<OrderDetailDTO> orderDetails;

    public OrderDetailsResponseDTO(Long orderId, String orderStatus, BigDecimal totalPrice, LocalDateTime deliveredDate, List<OrderDetailDTO> orderDetails) {
        this.orderId = orderId;
        this.orderStatus = orderStatus;
        this.totalPrice = totalPrice;
        this.deliveredDate = deliveredDate;
        this.orderDetails = orderDetails;
    }


    public static OrderDetailsResponseDTO fromEntity(Order order) {
        return new OrderDetailsResponseDTO(
                order.getOrderId(),
                order.getOrderStatus().getDescription(),
                order.getTotalPrice(),
                order.getDeliveredDate(),
                order.getOrderDetails().stream()
                        .map(OrderDetailDTO::fromEntity)
                        .collect(Collectors.toList())
        );
    }
}