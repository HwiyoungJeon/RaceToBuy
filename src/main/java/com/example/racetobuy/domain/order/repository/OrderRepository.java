package com.example.racetobuy.domain.order.repository;

import com.example.racetobuy.domain.order.entity.Order;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderIdAndMember_MemberId(Long orderId, Long memberId);

    List<Order> findAllByMember_MemberId(Long memberId, Sort sort);

}
