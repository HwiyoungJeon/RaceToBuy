package com.jh.productservice.service.product;


import com.jh.common.domain.page.PagedResponseDTO;
import com.jh.productservice.domain.product.dto.ProductWithEventDTO;

public interface  ProductService {

    /**
     * 커서 기반 페이지네이션을 사용하여 상품 목록을 조회합니다.
     *
     * @param cursor 현재 페이지를 결정하는 커서 값 (0이면 첫 페이지를 의미)
     * @param size   한 페이지에 표시할 상품 수
     * @return PagedResponseDTO<ProductWithEventDTO> 상품 목록 및 다음 커서 정보
     *
     * 사용 예:
     * - 페이지네이션을 통해 상품 데이터를 효율적으로 조회
     * - 클라이언트는 응답에 포함된 nextCursor를 사용하여 다음 페이지 요청 가능
     */
    PagedResponseDTO<ProductWithEventDTO> getProductsWithCursor(Long cursor, int size);

    /**
     * 특정 상품의 상세 정보를 조회합니다.
     *
     * @param productId 조회할 상품의 고유 ID
     * @return ProductWithEventDTO 상품의 상세 정보와 관련 이벤트 정보
     *
     * 사용 예:
     * - 상품 상세 페이지에서 특정 상품의 데이터 및 이벤트 정보를 표시
     * - 요청 시 존재하지 않는 상품 ID일 경우 적절한 예외 처리 필요
     */
    ProductWithEventDTO getProductById(Long productId);
}
