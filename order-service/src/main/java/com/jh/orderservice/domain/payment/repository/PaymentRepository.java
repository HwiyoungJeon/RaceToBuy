package com.jh.orderservice.domain.payment.repository;

import com.jh.common.constant.PaymentStatus;
import com.jh.orderservice.domain.order.entity.Order;
import com.jh.orderservice.domain.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByOrder(Order order);
    List<Payment> findByOrder_OrderId(Long orderId);
    boolean existsByOrder_OrderIdAndPaymentStatusIn(Long orderId, List<PaymentStatus> paymentStatuses);
}
