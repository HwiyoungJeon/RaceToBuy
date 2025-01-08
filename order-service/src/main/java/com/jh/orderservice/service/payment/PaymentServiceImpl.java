package com.jh.orderservice.service.payment;

import com.jh.common.constant.ErrorCode;
import com.jh.common.constant.PaymentFailureReason;
import com.jh.common.constant.PaymentStatus;
import com.jh.common.exception.BusinessException;
import com.jh.common.util.ApiResponse;
import com.jh.orderservice.domain.order.entity.Order;
import com.jh.orderservice.domain.order.repository.OrderRepository;
import com.jh.orderservice.domain.payment.dto.PaymentResponse;
import com.jh.orderservice.domain.payment.entity.Payment;
import com.jh.orderservice.domain.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    private static final Random RANDOM = new Random();
    private static final double PAYMENT_FAILURE_PROBABILITY = 0.2;  // 결제 실패 확률 20%
    private static final double PAYMENT_ATTEMPT_ABORT_PROBABILITY = 0.2;  // 결제 시도 중 이탈율 20%

    @Override
    @Transactional
    public ApiResponse<?> processPayment(Long orderId, String paymentMethod) {

        if (paymentRepository.existsByOrder_OrderIdAndPaymentStatusIn(orderId,
                List.of(PaymentStatus.PROCESSING, PaymentStatus.COMPLETED))) {
            return ApiResponse.fail(400, "이미 결제가 처리되었습니다.");
        }
        // 주문 조회
        Order order = findOrderById(orderId);

        // 결제 시도 중 이탈율 체크: 결제 시도 중 이탈하는 확률 20%
        if (RANDOM.nextDouble() < PAYMENT_ATTEMPT_ABORT_PROBABILITY) {
            // 결제 화면까지 왔지만 결제를 진행하지 않은 경우
            return ApiResponse.fail(400, "결제 화면에서 사용자가 이탈했습니다.");
        }

        // 결제 상태를 '처리 중'으로 설정
        Payment payment = Payment.builder()
                .order(order)
                .paymentMethod(paymentMethod)
                .paymentStatus(PaymentStatus.PROCESSING)  // 결제 처리 중 상태
                .failureReason(null)  // 결제 실패 사유는 null
                .build();

        log.debug("Payment Object: {}", payment);
//        paymentRepository.save(payment);

        // 20% 확률로 결제 실패 시뮬레이션
        if (RANDOM.nextDouble() < PAYMENT_FAILURE_PROBABILITY) {
            // 결제 실패 사유 랜덤 선택
            PaymentFailureReason failureReason = PaymentFailureReason.getRandomFailureReason();

            // 결제 상태를 '실패'로 변경하고, 실패 사유 설정
            payment = payment.withStatus(PaymentStatus.FAILED)
                    .withFailureReason(failureReason);
            paymentRepository.save(payment);

            return ApiResponse.paymentError(ErrorCode.PAYMENT_FAILURE,failureReason);
        }

        // 결제 완료 처리
        payment = payment.withStatus(PaymentStatus.COMPLETED)
                .withFailureReason(null);  // 결제 성공 시 실패 사유는 null로 설정
        paymentRepository.save(payment);

        // 주문 상태 업데이트: 결제 완료 후 ORDERED 상태로 변경
//        order.updateOrderStatus(OrderStatus.ORDERED);  // 주문 완료 상태로 변경
//        orderRepository.save(order);

        return ApiResponse.success("결제 완료");
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


}