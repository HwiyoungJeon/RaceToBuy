package com.jh.productservice.service.wishlist;


import com.jh.common.constant.ErrorCode;
import com.jh.common.domain.page.PagedResponseDTO;
import com.jh.common.exception.BusinessException;
import com.jh.common.util.ApiResponse;
import com.jh.productservice.domain.product.dto.EventInfoDTO;
import com.jh.productservice.domain.product.entity.Product;
import com.jh.productservice.domain.product.repository.ProductRepository;
import com.jh.productservice.domain.wishlist.dto.WishlistResponseDTO;
import com.jh.productservice.domain.wishlist.entity.Wishlist;
import com.jh.productservice.domain.wishlist.repository.WishlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WishlistServiceImpl implements WishlistService {

    private final WishlistRepository wishlistRepository;
    private final ProductRepository productRepository;

    @Override
    public WishlistResponseDTO addProductToWishlist(Long memberId, Long productId) {
//        상품 조회
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

//        중복 체크
        if (wishlistRepository.existsByMemberIdAndProduct_ProductId(memberId, productId)) {
            throw new BusinessException(ErrorCode.PRODUCT_ALREADY_IN_WISHLIST);
        }

//        위시리스트 저장
        Wishlist wishlist = Wishlist.createWishlist(memberId, product);

        wishlistRepository.save(wishlist);

       // WishlistResponseDTO 반환
        return new WishlistResponseDTO(
                wishlist.getWishlistId(),
                memberId,
                product.getProductId(),
                product.getProductName(),
                product.getPrice(),
                product.getStockQuantity(),
                product.getEventProducts().stream()
                        .map(eventProduct -> new EventInfoDTO(
                                eventProduct.getEvent().getEventId(),
                                eventProduct.getEvent().getEventName(),
                                eventProduct.getDiscountRate().doubleValue(),
                                product.getPrice()
                                        .multiply(BigDecimal.valueOf(eventProduct.getDiscountRate().doubleValue())
                                                .divide(BigDecimal.valueOf(100))),
                                product.getPrice()
                                        .subtract(product.getPrice()
                                                .multiply(BigDecimal.valueOf(eventProduct.getDiscountRate().doubleValue())
                                                        .divide(BigDecimal.valueOf(100))))
                        ))
                        .collect(Collectors.toList())
        );
    }

    @Override
    public ApiResponse<?> removeProductFromWishlist(Long memberId,Long wishlistId) {
        Wishlist wishlist = wishlistRepository.findByWishlistIdAndMemberId(wishlistId, memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.WISHLIST_ITEM_NOT_FOUND));

        wishlistRepository.delete(wishlist);

        return ApiResponse.success("위시리스트 삭제가 완료 됐습니다.");
    }

    @Override
    public PagedResponseDTO<WishlistResponseDTO> getWishlist(Long memberId, Long cursor, int size) {
//        커서가 0일 경우 1로 설정
        if (cursor == null || cursor <= 0) {
            cursor = 1L;
        }

//        현재 커서를 계산하여 시작 위치 결정
        int startIndex = (int) ((cursor - 1) * size);

//        회원의 위시리스트 조회 및 정렬
        List<Wishlist> wishlists = wishlistRepository.findAllByMemberId(memberId, Sort.by(Sort.Direction.ASC, "wishlistId"));

//        현재 커서에 맞는 데이터 필터링 (인덱스 기준 슬라이싱)
        List<Wishlist> paginatedWishlists = wishlists.stream()
                .skip(startIndex)    //커서 기반으로 시작점 이동
                 .limit(size)         //사이즈만큼 데이터 가져오기
                .toList();

//        Wishlist -> WishlistResponseDTO 변환
        List<WishlistResponseDTO> wishlistDtos = paginatedWishlists.stream()
                .map(wishlist -> {
                    Product product = wishlist.getProduct();

                    List<EventInfoDTO> events = product.getEventProducts().stream()
                            .map(eventProduct -> {
                                BigDecimal discountRate = BigDecimal.valueOf(eventProduct.getDiscountRate().doubleValue())
                                        .divide(BigDecimal.valueOf(100));

                                BigDecimal discountPrice = product.getPrice().multiply(BigDecimal.ONE.subtract(discountRate));
                                BigDecimal priceDifference = product.getPrice().subtract(discountPrice);

                                return new EventInfoDTO(
                                        eventProduct.getEvent().getEventId(),
                                        eventProduct.getEvent().getEventName(),
                                        eventProduct.getDiscountRate().doubleValue(),
                                        discountPrice,
                                        priceDifference
                                );
                            }).collect(Collectors.toList());

                    return new WishlistResponseDTO(
                            wishlist.getWishlistId(),
                            wishlist.getMemberId(),
                            product.getProductId(),
                            product.getProductName(),
                            product.getPrice(),
                            product.getStockQuantity(),
                            events
                    );
                })
                .collect(Collectors.toList());

//        다음 커서 계산
        boolean hasMore = wishlists.size() > startIndex + size;

        return new PagedResponseDTO<>(cursor, size, wishlistDtos,hasMore);
    }

    @Override
    @Transactional
    public ApiResponse<?> deleteAllByMemberMemberId(Long memberId) {
        //방어 로직: memberId 유효성 검사
        if (memberId == null || memberId < 0) {
            throw new BusinessException(ErrorCode.MEMBER_NOT_FOUND);
        }

        //위시리스트 삭제
        try {
            wishlistRepository.deleteAllByMemberId(memberId);
            return ApiResponse.success( "위시리스트의 모든 항목이 삭제되었습니다.");
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}