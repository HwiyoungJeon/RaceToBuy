package com.jh.productservice.domain.product.repository;

import com.jh.productservice.domain.product.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    boolean existsByEventName(String eventName);
}
