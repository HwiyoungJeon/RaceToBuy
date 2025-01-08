package com.jh.productservice.service.event;

import com.jh.common.util.ApiResponse;
import com.jh.productservice.domain.product.dto.EventAddRequest;
import com.jh.productservice.domain.product.dto.EventResponseDTO;

import java.time.LocalDateTime;
import java.util.List;

public interface EventService {
    ApiResponse<?> addEventToProduct(Long productId, Long eventId, Double discountRate);


    ApiResponse<?> createEvent(EventAddRequest request);

    List<EventResponseDTO> getAllEvents();  //

    ApiResponse<?> updateEventDates(Long eventId, LocalDateTime startDate, LocalDateTime endDate);
}
