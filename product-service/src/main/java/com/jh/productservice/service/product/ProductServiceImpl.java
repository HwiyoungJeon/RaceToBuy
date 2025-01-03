package com.jh.productservice.service.product;


import com.jh.common.constant.ErrorCode;
import com.jh.common.domain.page.PagedResponseDTO;
import com.jh.common.exception.BusinessException;
import com.jh.productservice.domain.product.dto.EventInfoDTO;
import com.jh.productservice.domain.product.dto.ProductWithEventDTO;
import com.jh.productservice.domain.product.entity.Event;
import com.jh.productservice.domain.product.entity.EventProduct;
import com.jh.productservice.domain.product.entity.Product;
import com.jh.productservice.domain.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    /**
     * 커서 기반 페이지네이션을 사용하여 상품 목록을 조회합니다.
     *
     * @param cursor 현재 페이지를 결정하는 커서 값 (0 이하일 경우 첫 페이지로 처리)
     * @param size   한 페이지에 표시할 상품 수
     * @return PagedResponseDTO<ProductWithEventDTO> 상품 목록 및 다음 커서 정보
     * <p>
     * 동작 방식:
     * - 전체 상품 데이터를 정렬하여 조회합니다.
     * - 커서 값과 페이지 크기를 기반으로 데이터를 슬라이싱합니다.
     * - 반환 데이터에 다음 커서를 포함하여 클라이언트가 다음 페이지를 요청할 수 있도록 합니다.
     */
    @Override
    public PagedResponseDTO<ProductWithEventDTO> getProductsWithCursor(Long cursor, int size) {
        // 커서가 0일 경우 1로 설정
        if (cursor == null || cursor <= 0) {
            cursor = 1L;
        }

        // 현재 커서를 계산하여 시작 위치 결정
        int startIndex = (int) ((cursor - 1) * size);

        // 데이터 전체를 정렬하여 가져오기
        List<Product> products = productRepository.findAll(Sort.by(Sort.Direction.ASC, "productId"));

        // 현재 커서에 맞는 데이터 필터링 (인덱스 기준 슬라이싱)
        List<Product> paginatedProducts = products.stream()
                .skip(startIndex)  // 커서 기반으로 시작점 이동
                .limit(size)       // 사이즈만큼 데이터 가져오기
                .collect(Collectors.toList());

        // 상품 목록 -> DTO 변환
        List<ProductWithEventDTO> productDtos = paginatedProducts.stream()
                .map(this::mapProductToDto)
                .collect(Collectors.toList());

        // 다음 커서 계산
        boolean hasMore = products.size() > startIndex + size;

        return new PagedResponseDTO<>(cursor, size, productDtos,hasMore);
    }

    /**
     * 특정 상품의 상세 정보를 조회합니다.
     *
     * @param productId 조회할 상품의 고유 ID
     * @return ProductWithEventDTO 상품의 상세 정보와 관련 이벤트 정보
     * <p>
     * 동작 방식:
     * - 상품 ID로 상품 데이터를 조회합니다.
     * - 상품이 존재하지 않을 경우 적절한 예외를 반환합니다.
     */
    @Override
    public ProductWithEventDTO getProductById(Long productId) {
        // 특정 상품 조회
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        // 상품 -> DTO 변환
        return mapProductToDto(product);
    }


    /**
     * 공통 매핑 로직: Product -> ProductWithEventDTO
     *
     * @param product 변환할 Product 엔티티
     * @return ProductWithEventDTO 변환된 상품 DTO
     * <p>
     * 동작 방식:
     * - 상품 엔티티를 DTO로 변환합니다.
     * - 관련된 이벤트 목록을 이벤트 DTO로 변환하여 포함합니다.
     */
    private ProductWithEventDTO mapProductToDto(Product product) {
        List<EventInfoDTO> events = product.getEventProducts().stream()
                .map(this::mapEventProductToEventInfoDto) // 이벤트 매핑 메서드 사용
                .collect(Collectors.toList());

        return new ProductWithEventDTO(
                product.getProductId(),
                product.getProductName(),
                product.getPrice(),
                product.getStockQuantity(),
                events
        );
    }

    /**
     * 공통 매핑 로직: EventProduct -> EventInfoDTO
     *
     * @param eventProduct 변환할 EventProduct 엔티티
     * @return EventInfoDTO 변환된 이벤트 DTO
     * <p>
     * 동작 방식:
     * - 이벤트와 상품 데이터를 사용하여 할인 가격과 차이를 계산합니다.
     * - 이벤트 정보를 DTO로 변환하여 반환합니다.
     */
    private EventInfoDTO mapEventProductToEventInfoDto(EventProduct eventProduct) {
        Event event = eventProduct.getEvent();
        Product product = eventProduct.getProduct();

        BigDecimal discountPrice = product.getPrice()
                .multiply(BigDecimal.valueOf(1 - (eventProduct.getDiscountRate().doubleValue() / 100)));
        BigDecimal priceDifference = product.getPrice().subtract(discountPrice);

        return new EventInfoDTO(
                event.getEventId(),
                event.getEventName(),
                eventProduct.getDiscountRate().doubleValue(),
                discountPrice,
                priceDifference
        );
    }
}