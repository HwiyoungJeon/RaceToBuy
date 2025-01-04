package com.jh.orderservice.aop;

import com.jh.common.constant.ErrorCode;
import com.jh.common.constant.OrderStatus;
import com.jh.common.exception.BusinessException;
import com.jh.common.util.ApiResponse;
import com.jh.orderservice.domain.order.entity.Order;
import com.jh.orderservice.domain.order.repository.OrderRepository;
import com.jh.orderservice.client.ProductServiceClient;
import com.jh.orderservice.client.dto.StockUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class OrderStatusAspect {

    private final OrderRepository orderRepository;
    private final ProductServiceClient productClient;

    @AfterReturning(pointcut = "execution(* com.jh.orderservice.service.OrderServiceImpl.updateOrderStatus(..)) || execution(* com.jh.orderservice.service.OrderServiceImpl.returnOrder(..))", returning = "result")
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
        if (result instanceof ApiResponse) {
            ApiResponse<?> apiResponse = (ApiResponse<?>) result;

            if (apiResponse.getData() instanceof Long) {
                return (Long) apiResponse.getData();
            }
        }

        return null;
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

}