package com.jh.productservice.controller;

import com.jh.common.domain.page.PagedResponseDTO;
import com.jh.common.util.ApiResponse;
import com.jh.productservice.domain.wishlist.dto.WishlistResponseDTO;
import com.jh.productservice.service.wishlist.WishlistService;
import com.jh.userservice.security.MemberDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
     * @param memberDetails 인증된 사용자 정보
     * @return WishlistResponseDTO
     */
    @PostMapping("/{productId}")
    public WishlistResponseDTO addProductToWishlist(
            @PathVariable Long productId,
            @AuthenticationPrincipal MemberDetails memberDetails) {
        return wishlistService.addProductToWishlist(memberDetails.getMemberId(), productId);
    }

    /**
     * 사용자의 위시리스트 조회
     *
     * @param memberDetails 인증된 사용자 정보
     * @return List<WishlistResponseDTO>
     */
    @GetMapping
    public PagedResponseDTO<WishlistResponseDTO> getWishlist(@AuthenticationPrincipal MemberDetails memberDetails,
                                                             @RequestParam(required = false, defaultValue = "0") Long cursor,
                                                             @RequestParam(defaultValue = "10") int size) {
        return wishlistService.getWishlist(memberDetails.getMemberId(), cursor, size);
    }

    /**
     * 위시리스트에서 상품 제거
     *
     * @param productId     제거할 상품 ID
     * @param memberDetails 인증된 사용자 정보
     */
    @DeleteMapping("/{productId}")
    public ResponseEntity<ApiResponse<?>> removeProductFromWishlist(
            @PathVariable Long productId,
            @AuthenticationPrincipal MemberDetails memberDetails) {
        ApiResponse<?> response = wishlistService.removeProductFromWishlist(memberDetails.getMemberId(), productId);

        return ResponseEntity.ok(response);
    }

    /**
     * 특정 회원의 모든 위시리스트 항목 삭제
     *
     * @param memberDetails 인증된 사용자 ID
     * @return ApiResponse<Void>
     */
    @DeleteMapping("/delete-all")
    public ApiResponse<?> deleteAllWishlist(@AuthenticationPrincipal MemberDetails memberDetails) {
        return wishlistService.deleteAllByMemberMemberId(memberDetails.getMemberId());
    }


}
