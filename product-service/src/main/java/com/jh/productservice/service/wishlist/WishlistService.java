package com.jh.productservice.service.wishlist;


import com.jh.common.domain.page.PagedResponseDTO;
import com.jh.common.util.ApiResponse;
import com.jh.productservice.domain.wishlist.dto.WishlistResponseDTO;

public interface WishlistService {

    /**
     * 위시리스트에 상품 추가
     *
     * @param memberId  회원 ID
     * @param productId 상품 ID
     * @return WishlistResponseDTO
     */
    WishlistResponseDTO addProductToWishlist(Long memberId, Long productId);

    /**
     * 위시리스트에서 상품 제거
     *
     * @param memberId  회원 ID
     * @param productId 상품 ID
     */
    ApiResponse<?> removeProductFromWishlist(Long memberId, Long productId);

    /**
     * 회원의 위시리스트 조회
     *
     * @param memberId 회원 ID
     * @return 위시리스트 리스트
     */
    PagedResponseDTO<WishlistResponseDTO> getWishlist(Long memberId, Long cursor, int size);

    /**
     * 특정 회원의 모든 위시리스트 항목 삭제
     *
     * @param memberId 회원 ID
     * @return ApiResponse<Void>
     */
    ApiResponse<?> deleteAllByMemberMemberId(Long memberId);
}