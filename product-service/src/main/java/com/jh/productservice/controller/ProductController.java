package com.jh.productservice.controller;

import com.jh.common.domain.page.PagedResponseDTO;
import com.jh.common.util.ApiResponse;
import com.jh.productservice.domain.product.dto.EventInfoDTO;
import com.jh.productservice.domain.product.dto.ProductWithEventDTO;
import com.jh.productservice.domain.product.dto.StockUpdateRequest;
import com.jh.productservice.service.product.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/products")
@Slf4j
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<PagedResponseDTO<ProductWithEventDTO>> getProductsWithCursor(
            @RequestParam(required = false, defaultValue = "0") Long cursor,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Received request for products. Cursor: {}, Size: {}", cursor, size);
        return ResponseEntity.ok(productService.getProductsWithCursor(cursor, size));
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponse<ProductWithEventDTO>> getProduct(@PathVariable Long productId) {
        log.info("Received request for product with id: {}", productId);
        try {
            ProductWithEventDTO product = productService.getProductById(productId);
            return ResponseEntity.ok(ApiResponse.success(product));
        } catch (Exception e) {
            log.error("Error fetching product: ", e);
            return ResponseEntity.ok(ApiResponse.successWithNoData());
        }
    }


    @GetMapping("/{productId}/check-stock")
    public ResponseEntity<Boolean>checkStock(
            @PathVariable Long productId,
            @RequestParam Integer quantity) {
        log.info("Checking stock for product: {}, quantity: {}", productId, quantity);
        boolean hasStock = productService.checkStock(productId, quantity);
        return ResponseEntity.ok(hasStock);
    }

    @PostMapping("/stock/decrease")
    public ResponseEntity<ApiResponse<Void>> decreaseStock(@RequestBody StockUpdateRequest request) {
        log.info("Decreasing stock for product: {}, quantity: {}",
                request.getProductId(), request.getQuantity());
        productService.decreaseStock(request.getProductId(), request.getQuantity());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/stock/increase")
    public ResponseEntity<ApiResponse<Void>> increaseStock(@RequestBody StockUpdateRequest request) {
        log.info("Increasing stock for product: {}, quantity: {}",
                request.getProductId(), request.getQuantity());
        productService.increaseStock(request.getProductId(), request.getQuantity());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/events/{eventId}")
    public ResponseEntity<ApiResponse<EventInfoDTO>> getEventInfo(
            @PathVariable Long eventId,
            @RequestParam Long productId) {
        log.info("Getting event info for event: {}, product: {}", eventId, productId);
        EventInfoDTO eventInfo = productService.getEventInfo(eventId, productId);
        return ResponseEntity.ok(ApiResponse.success(eventInfo));
    }
}
