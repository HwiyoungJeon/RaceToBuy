package com.example.racetobuy.service.wishlist;

import com.example.racetobuy.domain.member.entity.Member;
import com.example.racetobuy.domain.page.PagedResponseDTO;
import com.example.racetobuy.domain.product.dto.EventInfoDTO;
import com.example.racetobuy.domain.product.entity.Product;
import com.example.racetobuy.domain.product.repository.ProductRepository;
import com.example.racetobuy.domain.wishlist.dto.WishlistResponseDTO;
import com.example.racetobuy.domain.wishlist.entity.Wishlist;
import com.example.racetobuy.domain.wishlist.repository.WishlistRepository;
import com.example.racetobuy.global.constant.ErrorCode;
import com.example.racetobuy.global.exception.BusinessException;
import com.example.racetobuy.global.util.ApiResponse;
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
        // 상품 조회
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        // 중복 체크
        if (wishlistRepository.existsByMemberMemberIdAndProductProductId(memberId, productId)) {
            throw new BusinessException(ErrorCode.PRODUCT_ALREADY_IN_WISHLIST);
        }

        // 위시리스트 저장
        Wishlist wishlist = Wishlist.createWishlist(
                Member.builder().memberId(memberId).build(), product
        );

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
    public ApiResponse<?> removeProductFromWishlist(Long memberId, Long productId) {
        Wishlist wishlist = wishlistRepository.findByMemberMemberIdAndProductProductId(memberId, productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.WISHLIST_ITEM_NOT_FOUND));

        wishlistRepository.delete(wishlist);

        return ApiResponse.success("위시리스트 삭제가 완료 됐습니다.");
    }

    @Override
    public PagedResponseDTO<WishlistResponseDTO> getWishlist(Long memberId, Long cursor, int size) {
        // 커서가 0일 경우 1로 설정
        if (cursor == null || cursor <= 0) {
            cursor = 1L;
        }

        // 현재 커서를 계산하여 시작 위치 결정
        int startIndex = (int) ((cursor - 1) * size);

        // 회원의 위시리스트 조회 및 정렬
        List<Wishlist> wishlists = wishlistRepository.findAllByMemberMemberId(memberId, Sort.by(Sort.Direction.ASC, "wishlistId"));

        // 현재 커서에 맞는 데이터 필터링 (인덱스 기준 슬라이싱)
        List<Wishlist> paginatedWishlists = wishlists.stream()
                .skip(startIndex)  // 커서 기반으로 시작점 이동
                .limit(size)       // 사이즈만큼 데이터 가져오기
                .toList();

        // Wishlist -> WishlistResponseDTO 변환
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
                            wishlist.getMember().getMemberId(),
                            product.getProductId(),
                            product.getProductName(),
                            product.getPrice(),
                            product.getStockQuantity(),
                            events
                    );
                })
                .collect(Collectors.toList());

        // 다음 커서 계산
        boolean hasMore = wishlists.size() > startIndex + size;

        return new PagedResponseDTO<>(cursor, size, wishlistDtos,hasMore);
    }

    @Override
    @Transactional
    public ApiResponse<?> deleteAllByMemberMemberId(Long memberId) {
        // 방어 로직: memberId 유효성 검사
        if (memberId == null || memberId < 0) {
            throw new BusinessException(ErrorCode.MEMBER_NOT_FOUND);
        }

        // 위시리스트 삭제
        try {
            wishlistRepository.deleteAllByMember_MemberId(memberId);
            return ApiResponse.success( "위시리스트의 모든 항목이 삭제되었습니다.");
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}