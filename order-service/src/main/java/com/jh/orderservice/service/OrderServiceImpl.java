package com.jh.orderservice.service;


import com.jh.common.constant.ErrorCode;
import com.jh.common.constant.OrderStatus;
import com.jh.common.domain.page.PagedResponseDTO;
import com.jh.common.exception.BusinessException;
import com.jh.common.util.ApiResponse;
import com.jh.orderservice.client.MemberClient;
import com.jh.orderservice.client.MemberResponse;
import com.jh.orderservice.domain.order.dto.DayOffsetRequest;
import com.jh.orderservice.domain.order.dto.OrderDetailsResponseDTO;
import com.jh.orderservice.domain.order.dto.OrderRequestDTO;
import com.jh.orderservice.domain.order.dto.OrderResponseDTO;
import com.jh.orderservice.domain.order.entity.Order;
import com.jh.orderservice.domain.order.entity.OrderDetail;
import com.jh.orderservice.domain.order.repository.OrderDetailRepository;
import com.jh.orderservice.domain.order.repository.OrderRepository;
import com.jh.productservice.domain.product.entity.EventProduct;
import com.jh.productservice.domain.product.entity.Product;
import com.jh.productservice.domain.product.repository.EventProductRepository;
import com.jh.productservice.domain.product.repository.ProductRepository;
import com.jh.userservice.domain.repository.MemberRepository;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final ProductRepository productRepository;
    private final MemberClient memberClient;
    private final EventProductRepository eventProductRepository;

    @Override
    @Transactional
    public ApiResponse<?> createOrder(Long memberId, List<OrderRequestDTO> orderRequests) {
        MemberResponse member = findMemberById(memberId);

        Order order = createAndSaveOrder(member);

        BigDecimal totalPrice = processOrderDetails(order, orderRequests);

        order.updateTotalPrice(totalPrice);

        return ApiResponse.success("주문이 성공적으로 생성되었습니다.");
    }


    @Override
    @Transactional
    public ApiResponse<?> cancelOrder(Long memberId, Long orderId) {
        Order order = findOrderByIdAndMemberId(orderId, memberId);

        validateOrderStatusForCancellation(order);

        restoreStock(order);

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
            // memberId만으로 조회 (토큰 불필요)
            ApiResponse<MemberResponse> response = memberClient.getMemberById(memberId);
            return response.getData();
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

    private BigDecimal processOrderDetails(Order order, List<OrderRequestDTO> orderRequests) {
        BigDecimal totalPrice = BigDecimal.ZERO;

        for (OrderRequestDTO request : orderRequests) {
            System.out.println("Processing order getQuantity: " + request.getQuantity());
            System.out.println("Processing order getEventId: " + request.getEventId());
            System.out.println("Processing order getProductId: " + request.getProductId());
            Product product = findProductById(request.getProductId());

            validateStock(product, request.getQuantity());

            EventProduct appliedEvent = findEventProductIfApplicable(request.getEventId(), product);

            BigDecimal discountedPrice = calculateDiscountedPrice(product, appliedEvent);

            product.reduceStock(request.getQuantity());

            saveOrderDetail(order, product, request.getQuantity(), discountedPrice, appliedEvent);

            totalPrice = totalPrice.add(discountedPrice.multiply(BigDecimal.valueOf(request.getQuantity())));
        }

        return totalPrice;
    }

    private Product findProductById(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
    }

    private void validateStock(Product product, int quantity) {
        if (product.getStockQuantity() < quantity) {
            throw new BusinessException(ErrorCode.STOCK_NOT_ENOUGH);
        }
    }

    private EventProduct findEventProductIfApplicable(Long eventId, Product product) {
        if (eventId == null) return null;

        // 이벤트 조회
        EventProduct eventProduct = eventProductRepository.findByEvent_EventIdAndProduct_ProductId(eventId, product.getProductId())
                .orElseThrow(() -> new BusinessException(ErrorCode.EVENT_NOT_FOUND));

        // 이벤트와 상품 연결 여부 검증
        if (!eventProduct.getProduct().equals(product)) {
            throw new BusinessException(ErrorCode.EVENT_NOT_LINKED_TO_PRODUCT);
        }
        return eventProduct;
    }

    private BigDecimal calculateDiscountedPrice(Product product, EventProduct appliedEvent) {
        if (appliedEvent == null) return product.getPrice();

        BigDecimal discountRate = BigDecimal.valueOf(appliedEvent.getDiscountRate())
                .divide(BigDecimal.valueOf(100));

        return product.getPrice().multiply(BigDecimal.ONE.subtract(discountRate));
    }

    private void saveOrderDetail(Order order, Product product, int quantity, BigDecimal discountedPrice, EventProduct appliedEvent) {
        BigDecimal finalDiscountedPrice = discountedPrice != null ? discountedPrice : product.getPrice();

        Long eventProductId = null;
        String eventProductName = null;
        if (appliedEvent != null) {
            eventProductId = appliedEvent.getId(); // 이벤트가 있을 경우 ID를 가져옵니다.
            eventProductName = appliedEvent.getEvent().getEventName();
        }

        OrderDetail orderDetail = OrderDetail.builder()
                .order(order)
                .productId(product.getProductId())
                .productName(product.getProductName())
                .quantity(quantity)
                .price(finalDiscountedPrice.multiply(BigDecimal.valueOf(quantity)))
                .eventProductId(eventProductId)
                .eventProductName(eventProductName)
                .discountPrice(finalDiscountedPrice)
                .build();

        order.addOrderDetail(orderDetail);
        orderDetailRepository.save(orderDetail);
    }

    private void restoreStock(Order order) {
        order.getOrderDetails().forEach(detail -> {
            // productId를 사용하여 Product 데이터 조회
            Product product = productRepository.findById(detail.getProductId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

            // 재고 복구
            product.updateStockQuantity(product.getStockQuantity() + detail.getQuantity());

            // 변경된 Product 저장
            productRepository.save(product);
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
}
