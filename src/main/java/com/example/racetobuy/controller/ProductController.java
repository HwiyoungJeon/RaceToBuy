package com.example.racetobuy.controller;

import com.example.racetobuy.domain.page.PagedResponseDTO;
import com.example.racetobuy.domain.product.dto.ProductWithEventDTO;
import com.example.racetobuy.service.product.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    /**
     * 커서 기반 페이지네이션을 사용하여 상품 목록을 조회합니다.
     *
     * @param cursor 현재 페이지를 결정하는 커서 값 (0이면 첫 페이지를 의미)
     * @param size   한 페이지에 표시할 상품 수 (기본값: 10)
     * @return PagedResponseDTO<ProductWithEventDTO> 상품 목록 및 다음 커서 정보
     *
     * 예시 요청:
     * GET /products?cursor=1&size=5
     * 응답: 지정된 커서와 크기에 따라 상품 목록 반환
     */
    @GetMapping("/products")
    public PagedResponseDTO<ProductWithEventDTO> getProductsWithCursor(
            @RequestParam(required = false, defaultValue = "0") Long cursor,
            @RequestParam(defaultValue = "10") int size) {
        return productService.getProductsWithCursor(cursor, size);
    }


    /**
     * 특정 상품의 상세 정보를 조회합니다.
     *
     * @param productId 조회할 상품의 ID
     * @return ProductWithEventDTO 상품의 상세 정보와 관련 이벤트 정보
     *
     * 예시 요청:
     * GET /products/1
     * 응답: ID가 1인 상품의 상세 정보 반환
     */
    @GetMapping("/products/{productId}")
    public ProductWithEventDTO getProductById(@PathVariable Long productId) {
        return productService.getProductById(productId);
    }

}
