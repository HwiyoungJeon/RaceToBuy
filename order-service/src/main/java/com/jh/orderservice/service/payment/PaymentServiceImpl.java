package com.jh.orderservice.service.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jh.common.constant.ErrorCode;
import com.jh.common.constant.OrderStatus;
import com.jh.common.constant.PaymentFailureReason;
import com.jh.common.constant.PaymentStatus;
import com.jh.common.exception.BusinessException;
import com.jh.common.util.ApiResponse;
import com.jh.orderservice.client.ProductServiceClient;
import com.jh.orderservice.client.dto.StockUpdateRequest;
import com.jh.orderservice.domain.order.entity.Order;
import com.jh.orderservice.domain.order.entity.OrderDetail;
import com.jh.orderservice.domain.order.repository.OrderRepository;
import com.jh.orderservice.domain.payment.dto.PaymentResponse;
import com.jh.orderservice.domain.payment.dto.PaymentResponseDto;
import com.jh.orderservice.domain.payment.entity.Payment;
import com.jh.orderservice.domain.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final ProductServiceClient productServiceClient;


    private static final Random RANDOM = new Random();
    private static final double PAYMENT_FAILURE_PROBABILITY = 0.2;  // 결제 실패 확률 20%
    private static final double PAYMENT_ATTEMPT_ABORT_PROBABILITY = 0.2;  // 결제 시도 중 이탈율 20%

    private final RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String ORDER_KEY_PREFIX = "order:";

    @Override
    @Transactional
    public PaymentResponseDto processPayment(Long orderId, String paymentMethod) {

        // 이미 결제가 처리된 경우
        if (paymentRepository.existsByOrder_OrderIdAndPaymentStatusIn(orderId,
                List.of(PaymentStatus.COMPLETED))) {

            Payment payment = paymentRepository.findByOrder_OrderId(orderId)
                    .stream()
                    .findFirst()
                    .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));

            return new PaymentResponseDto(orderId, payment.getPaymentStatus(), "이미 결제가 처리되었습니다.");
        }

        getOrderFromRedis(orderId);

        // 주문 조회
        Order order = findOrderById(orderId);

        // 결제 시도 중 이탈율 체크: 결제 시도 중 이탈하는 확률 20%
        if (RANDOM.nextDouble() < PAYMENT_ATTEMPT_ABORT_PROBABILITY) {
            // 결제 화면까지 왔지만 결제를 진행하지 않은 경우
            return new PaymentResponseDto(orderId, PaymentStatus.PROCESSING, "결제 화면에서 사용자가 이탈했습니다.");
        }

        // 결제 상태를 '처리 중'으로 설정
        Payment payment = Payment.builder()
                .order(order)
                .paymentMethod(paymentMethod)
                .paymentStatus(PaymentStatus.PROCESSING)
                .failureReason(null)
                .build();

        log.debug("Payment Object: {}", payment);

        // 20% 확률로 결제 실패 시뮬레이션
        if (RANDOM.nextDouble() < PAYMENT_FAILURE_PROBABILITY) {
            // 결제 실패 사유 랜덤 선택
            PaymentFailureReason failureReason = PaymentFailureReason.getRandomFailureReason();

            switch (failureReason) {
                case CARD_EXPIRED, INSUFFICIENT_FUNDS, PAYMENT_LIMIT_EXCEEDED -> {
                    order.updateOrderStatus(OrderStatus.ORDER_PENDING);
                    payment = payment.withStatus(PaymentStatus.FAILED)
                            .withFailureReason(failureReason);
                }
                case NETWORK_ERROR, SERVICE_UNAVAILABLE, OTHER -> {
                    order.updateOrderStatus(OrderStatus.FAILED);
                    payment = payment.withStatus(PaymentStatus.FAILED)
                            .withFailureReason(failureReason);
                }
                case USER_CANCELLED -> {
                    order.updateOrderStatus(OrderStatus.CANCELLED);
                    payment = payment.withStatus(PaymentStatus.FAILED)
                            .withFailureReason(failureReason);
                }
            }

            // 결제 상태를 '실패'로 변경하고, 실패 사유 설정
            paymentRepository.save(payment);
            orderRepository.save(order);

            return new PaymentResponseDto(orderId, PaymentStatus.FAILED, failureReason.getDescription());
        }

        // 결제 완료 처리
        payment = payment.withStatus(PaymentStatus.COMPLETED)
                .withFailureReason(null);  // 결제 성공 시 실패 사유는 null로 설정

        paymentRepository.save(payment);

        order.updateOrderStatus(OrderStatus.ORDERED);
        orderRepository.save(order);

        try {
            for (OrderDetail detail : order.getOrderDetails()) {
                // 각 주문 항목에 대해 재고 차감
                StockUpdateRequest request = StockUpdateRequest.builder()
                        .productId(detail.getProductId())
                        .quantity(detail.getQuantity())
                        .build();
                ApiResponse<Boolean> response = productServiceClient.decreaseStock(request);

                if (response == null || !response.getData()) {
                    throw new BusinessException(ErrorCode.STOCK_DECREASE_FAILED);
                }
            }
        } catch (BusinessException e) {
            // 재고 차감 실패 시 롤백
            order.updateOrderStatus(OrderStatus.FAILED);
            payment = payment.withStatus(PaymentStatus.FAILED)
                    .withFailureReason(PaymentFailureReason.OTHER);
            paymentRepository.save(payment);
            orderRepository.save(order);
            return new PaymentResponseDto(orderId, PaymentStatus.FAILED, "재고 부족으로 결제 실패");
        }

        // 결제 완료 상태를 반환
        return new PaymentResponseDto(orderId, PaymentStatus.COMPLETED, null);
    }

    /**
     * 결제 상태 조회
     * @param orderId 주문 ID
     * @return 결제 정보
     */
    @Override
    public ApiResponse<?> getAllPaymentsForOrder(Long orderId) {
        List<Payment> payments = paymentRepository.findByOrder_OrderId(orderId);

        // 결제 정보가 없다면 예외 처리
        if (payments.isEmpty()) {
            throw new BusinessException(ErrorCode.PAYMENT_NOT_FOUND);
        }

        List<PaymentResponse> paymentResponses = convertToPaymentResponse(payments);
        return ApiResponse.success(paymentResponses);
    }


    private Order findOrderById(Long orderId) {
        // 주문 조회
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
    }

    // Payment 엔티티를 PaymentResponse DTO로 변환
    private List<PaymentResponse> convertToPaymentResponse(List<Payment> payments) {
        return payments.stream()
                .map(PaymentResponse::from)
                .collect(Collectors.toList());
    }

    private Order getOrderFromRedis(Long orderId) {
        // Redis에서 데이터를 가져옵니다.
        Object orderFromRedis = redisTemplate.opsForValue().get(ORDER_KEY_PREFIX + orderId);

        if (orderFromRedis == null) {
            throw new BusinessException(ErrorCode.INVALID_PAYMENT_REQUEST);
        }

        try {
            // JSON으로 직렬화된 데이터를 Order 객체로 변환
            return objectMapper.convertValue(orderFromRedis, Order.class);
        } catch (Exception e) {
            // 역직렬화 실패 시 처리
            throw new BusinessException(ErrorCode.INVALID_PAYMENT_REQUEST);
        }
    }
}