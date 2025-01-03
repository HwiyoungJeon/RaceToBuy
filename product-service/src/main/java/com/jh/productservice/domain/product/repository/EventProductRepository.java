package com.jh.productservice.domain.product.repository;

import com.jh.productservice.domain.product.entity.EventProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EventProductRepository extends JpaRepository<EventProduct, Long> {
    Optional<EventProduct> findByEvent_EventIdAndProduct_ProductId(Long eventId, Long productId);
}
