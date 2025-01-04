package com.jh.productservice.domain.wishlist.entity;

import com.jh.common.domain.timestamp.TimeStamp;
import com.jh.productservice.domain.product.entity.Product;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "wishlist")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Wishlist extends TimeStamp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "wishlist_id")
    private Long wishlistId;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    /**
     * 위시리스트 생성 메서드
     *
     * @param memberId 위시리스트 소유 회원 ID
     * @param product 위시리스트에 등록할 상품
     * @return Wishlist 객체
     */
    @Builder
    public static Wishlist createWishlist(Long memberId, Product product) {
        Wishlist wishlist = new Wishlist();
        wishlist.memberId = memberId;
        wishlist.product = product;
        return wishlist;
    }
}