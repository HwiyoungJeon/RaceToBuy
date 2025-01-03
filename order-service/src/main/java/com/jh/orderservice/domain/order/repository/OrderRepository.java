package com.jh.orderservice.domain.order.repository;

import com.jh.orderservice.domain.order.entity.Order;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderIdAndMemberId(Long orderId, Long memberId);

    List<Order> findAllByMemberId(Long memberId, Sort sort);

}
