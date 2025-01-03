package com.jh.productservice.domain.wishlist.dto;

import com.jh.productservice.domain.product.dto.EventInfoDTO;

import java.math.BigDecimal;
import java.util.List;

public record WishlistResponseDTO(
        Long wishlistId,             //위시리스트 ID
        Long userId,                 //유저 ID
        Long productId,              //상품 ID
        String productName,          //상품 이름
        BigDecimal price,            //상품 가격
        Integer stockQuantity,       //재고 수량
        List<EventInfoDTO> events    //이벤트 정보
) {
}
