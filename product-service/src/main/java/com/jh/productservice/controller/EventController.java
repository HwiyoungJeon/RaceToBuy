package com.jh.productservice.controller;

import com.jh.common.util.ApiResponse;
import com.jh.productservice.domain.product.dto.*;
import com.jh.productservice.service.event.EventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/events")
@Slf4j
public class EventController {

    private final EventService eventService;

    @PostMapping("/event-product")
    public ResponseEntity<ApiResponse<?>> addEventToProduct(@RequestBody EventProductAddRequest request) {
        ApiResponse<?> response = eventService.addEventToProduct(request.getProductId(), request.getEventId(), request.getDiscountRate());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<?>> createEvent(@RequestBody EventAddRequest request) {
        log.info("Creating event with name: {}, discountRate: {}, startDate: {}, endDate: {}",
                request.getEventName(), request.getDiscountRate(), request.getStartDate(), request.getEndDate());

        ApiResponse<?> response = eventService.createEvent(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("")
    public ResponseEntity<ApiResponse<List<EventResponseDTO>>> getAllEvents() {
        List<EventResponseDTO> events = eventService.getAllEvents();
        return ResponseEntity.ok(ApiResponse.success(events));
    }

    @PutMapping("/dates")
    public ResponseEntity<ApiResponse<?>> updateEventDates(
            @RequestBody EventDateUpdateRequest request) {  // DTO로 받음

        // id를 받아서 서비스로 전달
        ApiResponse<?> response = eventService.updateEventDates(request.getId(), request.getStartDate(), request.getEndDate());
        return ResponseEntity.ok(response);
    }
}
