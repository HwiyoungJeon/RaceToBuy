package com.jh.orderservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jh.common.constant.ErrorCode;
import com.jh.common.constant.OrderStatus;
import com.jh.common.domain.page.PagedResponseDTO;
import com.jh.common.exception.BusinessException;
import com.jh.common.util.ApiResponse;
import com.jh.orderservice.client.MemberClient;
import com.jh.orderservice.client.MemberResponse;
import com.jh.orderservice.client.ProductServiceClient;
import com.jh.orderservice.client.dto.EventInfoDTO;
import com.jh.orderservice.client.dto.ProductResponse;
import com.jh.orderservice.client.dto.StockUpdateRequest;
import com.jh.orderservice.domain.order.dto.DayOffsetRequest;
import com.jh.orderservice.domain.order.dto.OrderDetailsResponseDTO;
import com.jh.orderservice.domain.order.dto.OrderRequestDTO;
import com.jh.orderservice.domain.order.dto.OrderResponseDTO;
import com.jh.orderservice.domain.order.entity.Order;
import com.jh.orderservice.domain.order.entity.OrderDetail;
import com.jh.orderservice.domain.order.repository.OrderDetailRepository;
import com.jh.orderservice.domain.order.repository.OrderRepository;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final MemberClient memberClient;
    private final ProductServiceClient productClient;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public ApiResponse<?> createOrder(Long memberId, List<OrderRequestDTO> orderRequests) {
        MemberResponse member = findMemberById(memberId);
        Order order = createAndSaveOrder(member);

        processOrderDetails(order, orderRequests);

        return ApiResponse.success("주문이 성공적으로 생성되었습니다.");
    }

    private void processOrderDetails(Order order, List<OrderRequestDTO> orderRequests) {
        BigDecimal totalPrice = BigDecimal.ZERO;

        for (OrderRequestDTO request : orderRequests) {
            // 상품 정보 조회
            ProductResponse product = findProductById(request.getProductId());

            // 재고 확인
            try {
                ApiResponse<Boolean> stockCheckResponse =
                        productClient.checkStock(product.getProductId(), request.getQuantity());
                if (!Boolean.TRUE.equals(stockCheckResponse.getData())) {
                    throw new BusinessException(ErrorCode.STOCK_NOT_ENOUGH);
                }
            } catch (FeignException e) {
                throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND);
            }
            // 이벤트 적보 조회 및 가격 계산
            BigDecimal finalPrice;
            if (request.getEventId() != null) {
                try {
                    EventInfoDTO eventInfo = productClient.getEventInfo(request.getEventId(), product.getProductId()).getData();
                    finalPrice = eventInfo.getDiscountPrice();
                } catch (FeignException e) {
                    throw new BusinessException(ErrorCode.EVENT_NOT_FOUND);
                }
            } else {
                finalPrice = product.getPrice();
            }

            // 주문 상세 생성
            OrderDetail orderDetail = OrderDetail.builder()
                    .order(order)
                    .productId(product.getProductId())
                    .productName(product.getProductName())
                    .quantity(request.getQuantity())
                    .price(finalPrice.multiply(BigDecimal.valueOf(request.getQuantity())))
//                    .productSnapshot(createProductSnapshot(product))
                    .build();

            order.addOrderDetail(orderDetail);
            orderDetailRepository.save(orderDetail);

            // 재고 감소 요청
            productClient.decreaseStock(new StockUpdateRequest(
                    product.getProductId(),
                    request.getQuantity()
            ));

            totalPrice = totalPrice.add(finalPrice.multiply(BigDecimal.valueOf(request.getQuantity())));
        }

        order.updateTotalPrice(totalPrice);
    }

    private String createProductSnapshot(ProductResponse product) {
        try {
            return objectMapper.writeValueAsString(product);
        } catch (JsonProcessingException e) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    private ProductResponse getProductFromSnapshot(String snapshot) {
        try {
            return objectMapper.readValue(snapshot, ProductResponse.class);
        } catch (JsonProcessingException e) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @Transactional
    public ApiResponse<?> cancelOrder(Long memberId, Long orderId) {
        Order order = findOrderByIdAndMemberId(orderId, memberId);
        validateOrderStatusForCancellation(order);

        // 재고 복구 요청
        order.getOrderDetails().forEach(detail -> {
            productClient.increaseStock(new StockUpdateRequest(
                    detail.getProductId(),
                    detail.getQuantity()
            ));
        });

        order.cancelOrder();
        return ApiResponse.success("주문이 성공적으로 취소되었습니다.");
    }

    @Override
    @Transactional
    public ApiResponse<?> returnOrder(Long memberId, Long orderId) {
        Order order = findOrderByIdAndMemberId(orderId, memberId);

        // 반품 가능 여부 확인
        if (!OrderStatus.DELIVERED.equals(order.getOrderStatus()) && !OrderStatus.DELIVERED_DAY1.equals(order.getOrderStatus())) {
            throw new BusinessException(ErrorCode.RETURN_NOT_ALLOWED_NOT_DELIVERED);
        }

        if (order.getDayOffset() > 3) { // D+1 이후 반품 불가
            throw new BusinessException(ErrorCode.RETURN_PERIOD_EXPIRED);
        }
        order.requestReturn();

        restoreStock(order);

        order.completeReturn();

        return ApiResponse.success("반품 처리가 완료되었습니다.");
    }

//    @Override
//    @Transactional
//    public ApiResponse<?> markOrderAsDelivered(Long orderId) {
//        Order order = orderRepository.findById(orderId)
//                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
//
//        order.markAsDelivered();
//
//        return ApiResponse.success("배송이 완료되었습니다.");
//    }

    // Helper Methods

//    private Member findMemberById(Long memberId) {
//        return memberRepository.findById(memberId)
//                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
//    }

    public MemberResponse findMemberById(Long memberId) {
        try {
            ApiResponse<MemberResponse> response = memberClient.getMemberById(memberId);
            return response.getData();  // ApiResponse에서 실제 데이터 추출
        } catch (FeignException.NotFound e) {
            throw new BusinessException(ErrorCode.MEMBER_NOT_FOUND);
        }
    }

    private Order findOrderByIdAndMemberId(Long orderId, Long memberId) {
        return orderRepository.findByOrderIdAndMemberId(orderId, memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
    }

    private Order createAndSaveOrder(MemberResponse member) {
        Order order = Order.builder()
                .memberId(member.getId())
                .orderStatus(OrderStatus.ORDERED)
                .totalPrice(BigDecimal.ZERO)
                .dayOffset(0) // 초기값 설정
                .build();

        return orderRepository.save(order);
    }

    private void restoreStock(Order order) {
        order.getOrderDetails().forEach(detail -> {
            productClient.increaseStock(new StockUpdateRequest(
                    detail.getProductId(),
                    detail.getQuantity()
            ));
        });
    }


    private void validateOrderStatusForCancellation(Order order) {
        if (!OrderStatus.ORDERED.equals(order.getOrderStatus())) {
            throw new BusinessException(ErrorCode.ORDER_CANCELLATION_NOT_ALLOWED);
        }
    }

    @Override
    @Transactional
    public ApiResponse<?> updateOrderStatus(Long orderId, DayOffsetRequest dayOffsetRequest) {
        // 주문 조회
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        // dayOffset 값 설정
        order.updateSetDayOffset(dayOffsetRequest.getDayOffset());

        // dayOffset 기반 상태 변경
        switch (dayOffsetRequest.getDayOffset()) {
            case 0 -> order.updateOrderStatus(OrderStatus.ORDERED); // 초기 상태
            case 1 -> order.updateOrderStatus(OrderStatus.SHIPPING); // 배송중
            case 2 -> order.updateOrderStatus(OrderStatus.DELIVERED); // 배송 완료
            case 3 -> order.updateOrderStatus(OrderStatus.DELIVERED_DAY1);
            case 4 -> order.updateOrderStatus(OrderStatus.DELIVERED_NOT_FOUNT);
            default -> {
                if (dayOffsetRequest.getDayOffset() >= 5) {
                    throw new BusinessException(ErrorCode.EVENT_SERVICE_EXPIRED); // 4 이상이면 에러 처리
                }
                throw new BusinessException(ErrorCode.INVALID_ORDER_STATUS); // 그 외 에러 처리
            }
        }

        // 상태 업데이트 후 저장
        orderRepository.save(order);

        return ApiResponse.success("주문 상태가 성공적으로 업데이트되었습니다.");
    }

    // Helper Method
    private OrderStatus validateAndConvertStatus(String status) {
        try {
            return OrderStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.INVALID_ORDER_STATUS);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<?> getOrdersByMemberId(Long memberId, Long cursor, int size) {
        // 커서가 0일 경우 1로 설정
        if (cursor == null || cursor <= 0) {
            cursor = 1L;
        }

        int startIndex = (int) ((cursor - 1) * size);

        List<Order> allOrders = orderRepository.findAllByMemberId(memberId, Sort.by(Sort.Direction.DESC, "createdAt"));

        // 슬라이싱: 시작 인덱스에서 size만큼 데이터 가져오기
        List<Order> paginatedOrders = allOrders.stream()
                .skip(startIndex)
                .limit(size)
                .toList();

        // DTO 변환
        List<OrderResponseDTO> orderResponses = paginatedOrders.stream()
                .map(OrderResponseDTO::fromEntity)
                .collect(Collectors.toList());

        // 다음 커서 계산
        boolean hasMore = allOrders.size() > startIndex + size;


        return ApiResponse.success(new PagedResponseDTO<>(
                cursor,
                size,
                orderResponses,
                hasMore
        ));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<?> getOrderDetailsById(Long orderId, Long memberId) {
        Order order = findOrderByIdAndMemberId(orderId, memberId);

        OrderDetailsResponseDTO orderDetailsResponse = OrderDetailsResponseDTO.fromEntity(order);

        return ApiResponse.success(orderDetailsResponse);
    }

    private ProductResponse findProductById(Long productId) {
        try {
            ApiResponse<ProductResponse> response = productClient.getProduct(productId);
            if (response.getData() == null) {
                throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND);
            }
            return response.getData();
        } catch (FeignException e) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND);
        }
    }
}
