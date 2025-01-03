package com.jh.productservice.controller;

import com.jh.common.domain.page.PagedResponseDTO;
import com.jh.productservice.domain.product.dto.ProductWithEventDTO;
import com.jh.productservice.service.product.ProductService;
import com.jh.userservice.security.MemberDetails;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Slf4j
public class ProductController {

    private final ProductService productService;

    @GetMapping("/products")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PagedResponseDTO<ProductWithEventDTO>> getProductsWithCursor(
            @RequestParam(required = false, defaultValue = "0") Long cursor,
            @RequestParam(defaultValue = "10") int size) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        MemberDetails memberDetails = (MemberDetails) auth.getPrincipal();
        System.out.println("memerId : " + memberDetails.getMemberId());

        log.info("Received request for products list. Cursor: {}, Size: {}", cursor, size);
        try {
            PagedResponseDTO<ProductWithEventDTO> response = productService.getProductsWithCursor(cursor, size);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error retrieving products: ", e);
            throw e;
        }
    }

    @GetMapping("/products/{productId}")
    public ResponseEntity<ProductWithEventDTO> getProductById(@PathVariable Long productId) {
        log.info("Received request for product with id: {}", productId);
        try {
            ProductWithEventDTO product = productService.getProductById(productId);
            if (product == null) {
                throw new EntityNotFoundException("Product not found with id: " + productId);
            }
            log.info("Successfully retrieved product: {}", productId);
            return ResponseEntity.ok(product);
        } catch (Exception e) {
            log.error("Error retrieving product with id {}: ", productId, e);
            throw e;
        }
    }
}
