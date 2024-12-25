package com.example.racetobuy.domain.order.dto;

import com.example.racetobuy.domain.order.entity.OrderDetail;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class OrderDetailDTO {
    private Long productId;          // 상품 ID
    private String productName;      // 상품명
    private Integer quantity;        // 수량
    private BigDecimal price;        // 상품 가격
    private Long eventId;            // 이벤트 ID
    private String eventName;        // 이벤트명
    private BigDecimal discountPrice; // 할인 적용 금액

    public static OrderDetailDTO fromEntity(OrderDetail orderDetail) {
        return new OrderDetailDTO(
                orderDetail.getProduct().getProductId(),
                orderDetail.getProduct().getProductName(),
                orderDetail.getQuantity(),
                orderDetail.getPrice(),
                orderDetail.getEventProduct() != null ? orderDetail.getEventProduct().getEvent().getEventId() : null,
                orderDetail.getEventProduct() != null ? orderDetail.getEventProduct().getEvent().getEventName() : null,
                orderDetail.getDiscountPrice()
        );
    }
}
