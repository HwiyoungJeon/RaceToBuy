package com.example.racetobuy.service.order;

import com.example.racetobuy.domain.member.entity.Member;
import com.example.racetobuy.domain.member.repository.MemberRepository;
import com.example.racetobuy.domain.order.dto.DayOffsetRequest;
import com.example.racetobuy.domain.order.dto.OrderDetailsResponseDTO;
import com.example.racetobuy.domain.order.dto.OrderRequestDTO;
import com.example.racetobuy.domain.order.dto.OrderResponseDTO;
import com.example.racetobuy.domain.order.entity.Order;
import com.example.racetobuy.domain.order.entity.OrderDetail;
import com.example.racetobuy.domain.order.repository.OrderDetailRepository;
import com.example.racetobuy.domain.order.repository.OrderRepository;
import com.example.racetobuy.domain.page.PagedResponseDTO;
import com.example.racetobuy.domain.product.entity.EventProduct;
import com.example.racetobuy.domain.product.entity.Product;
import com.example.racetobuy.domain.product.repository.EventProductRepository;
import com.example.racetobuy.domain.product.repository.ProductRepository;
import com.example.racetobuy.global.constant.ErrorCode;
import com.example.racetobuy.global.constant.OrderStatus;
import com.example.racetobuy.global.exception.BusinessException;
import com.example.racetobuy.global.util.ApiResponse;
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
    private final MemberRepository memberRepository;
    private final EventProductRepository eventProductRepository;

    @Override
    @Transactional
    public ApiResponse<?> createOrder(Long memberId, List<OrderRequestDTO> orderRequests) {
        Member member = findMemberById(memberId);

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

    private Member findMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
    }

    private Order findOrderByIdAndMemberId(Long orderId, Long memberId) {
        return orderRepository.findByOrderIdAndMember_MemberId(orderId, memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
    }

    private Order createAndSaveOrder(Member member) {
        Order order = Order.builder()
                .member(member)
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

        OrderDetail orderDetail = OrderDetail.builder()
                .order(order)
                .product(product)
                .quantity(quantity)
                .price(finalDiscountedPrice.multiply(BigDecimal.valueOf(quantity)))
                .eventProduct(appliedEvent)
                .discountPrice(finalDiscountedPrice)
                .build();

        order.addOrderDetail(orderDetail);
        orderDetailRepository.save(orderDetail);
    }

    private void restoreStock(Order order) {
        order.getOrderDetails().forEach(detail -> {
            Product product = detail.getProduct();
            product.updateStockQuantity(product.getStockQuantity() + detail.getQuantity());
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

        List<Order> allOrders = orderRepository.findAllByMember_MemberId(memberId, Sort.by(Sort.Direction.DESC, "createdAt"));

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
