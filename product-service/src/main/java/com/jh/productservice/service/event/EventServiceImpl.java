package com.jh.productservice.service.event;


import com.jh.common.constant.ErrorCode;
import com.jh.common.exception.BusinessException;
import com.jh.common.util.ApiResponse;
import com.jh.productservice.domain.product.dto.EventAddRequest;
import com.jh.productservice.domain.product.dto.EventResponseDTO;
import com.jh.productservice.domain.product.entity.Event;
import com.jh.productservice.domain.product.entity.EventProduct;
import com.jh.productservice.domain.product.entity.Product;
import com.jh.productservice.domain.product.repository.EventProductRepository;
import com.jh.productservice.domain.product.repository.EventRepository;
import com.jh.productservice.domain.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class EventServiceImpl implements EventService {

    private final ProductRepository productRepository;
    private final EventProductRepository eventProductRepository;
    private final EventRepository eventRepository;

    @Override
    @Transactional
    public ApiResponse<?> addEventToProduct(Long productId, Long eventId, Double discountRate) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EVENT_NOT_FOUND));

        boolean alreadyExists = eventProductRepository.existsByProduct_ProductIdAndEvent_EventId(productId, eventId);
        if (alreadyExists) {
            // 이미 연결되어 있으면 예외를 발생시킴
            throw new BusinessException(ErrorCode.EVENT_ALREADY_EXISTS);
        }
        EventProduct eventProduct = new EventProduct(event, product, discountRate);
        eventProductRepository.save(eventProduct);

        return ApiResponse.success(eventProduct);
    }

    @Override
    public ApiResponse<?> createEvent(EventAddRequest request) {
        // 이미 존재하는 이벤트 이름 확인 (선택적)
        if (eventRepository.existsByEventName(request.getEventName())) {
            throw new BusinessException(ErrorCode.EVENT_ALREADY_EXISTS);
        }

        // 유효한 날짜 범위 확인 (종료일자가 시작일자 이후인지 체크)
        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new BusinessException(ErrorCode.INVALID_DATE_RANGE);
        }

        // 새로운 이벤트 생성
        Event event = new Event(request.getEventName(), request.getStartDate(), request.getEndDate());
        // 이벤트 저장
        eventRepository.save(event);

        return ApiResponse.success(event);
    }

    @Override
    public List<EventResponseDTO> getAllEvents() {
        // 전체 이벤트를 조회
        List<Event> events = eventRepository.findAll();

        // 이벤트 데이터를 DTO로 변환
        // 이벤트 데이터를 DTO로 변환
        return events.stream()
                .map(event -> {
                    // EventProduct와 연결된 discountRate 가져오기
                    EventProduct eventProduct = event.getEventProducts().stream().findFirst().orElse(null);
                    Double discountRate = (eventProduct != null) ? eventProduct.getDiscountRate() : null;

                    return new EventResponseDTO(
                            event.getEventId(),
                            event.getEventName(),
                            discountRate,  // 이벤트에 연결된 상품의 할인율
                            event.getStartDate(),
                            event.getEndDate()
                    );
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public  ApiResponse<?> updateEventDates(Long eventId, LocalDateTime startDate, LocalDateTime endDate) {
        // 이벤트를 ID로 조회
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EVENT_NOT_FOUND));

        // 시작일자와 종료일자가 유효한지 확인
        if (startDate.isAfter(endDate)) {
            throw new BusinessException(ErrorCode.INVALID_DATE_RANGE);
        }

        // 시작일자와 종료일자 업데이트
       event.updateDates(startDate, endDate);
        // 변경된 이벤트를 저장
        eventRepository.save(event);

        return ApiResponse.success(event);
    }
}
