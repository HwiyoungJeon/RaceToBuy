package com.jh.productservice.controller;

import com.jh.common.domain.page.PagedResponseDTO;
import com.jh.common.util.ApiResponse;
import com.jh.productservice.domain.wishlist.dto.WishlistResponseDTO;
import com.jh.productservice.service.wishlist.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/wishlist")
public class WishlistController {

    private final WishlistService wishlistService;

    /**
     * 위시리스트에 상품 추가
     *
     * @param productId     추가할 상품 ID
     * @param memberId 인증된 사용자 정보
     * @return WishlistResponseDTO
     */
    @PostMapping("/{productId}")
    public WishlistResponseDTO addProductToWishlist(
            @RequestHeader("X-Authorization-Id") Long memberId,
            @PathVariable Long productId) {
        return wishlistService.addProductToWishlist(memberId, productId);
    }

    /**
     * 사용자의 위시리스트 조회
     *
     * @param memberId 인증된 사용자 정보
     * @return List<WishlistResponseDTO>
     */
    @GetMapping
    public PagedResponseDTO<WishlistResponseDTO> getWishlist(
            @RequestHeader("X-Authorization-Id") Long memberId,
            @RequestParam(required = false, defaultValue = "0") Long cursor,
            @RequestParam(defaultValue = "10") int size) {
        return wishlistService.getWishlist(memberId, cursor, size);
    }

    /**
     * 위시리스트에서 상품 제거
     *
     * @param wishlistId     제거할 상품 ID
     * @param memberId 인증된 사용자 정보
     */
    @DeleteMapping("/{wishlistId}")
    public ResponseEntity<ApiResponse<?>> removeProductFromWishlist(
            @RequestHeader("X-Authorization-Id") Long memberId,
            @PathVariable Long wishlistId) {
        ApiResponse<?> response = wishlistService.removeProductFromWishlist(memberId, wishlistId);

        return ResponseEntity.ok(response);
    }

    /**
     * 특정 회원의 모든 위시리스트 항목 삭제
     *
     * @param memberId 인증된 사용자 ID
     * @return ApiResponse<Void>
     */
    @DeleteMapping("/delete-all")
    public  ResponseEntity<ApiResponse<?>>  deleteAllWishlist(
            @RequestHeader("X-Authorization-Id") Long memberId
    ) {
        ApiResponse<?> response = wishlistService.deleteAllByMemberMemberId(memberId);
        return ResponseEntity.ok(response);
    }


}
