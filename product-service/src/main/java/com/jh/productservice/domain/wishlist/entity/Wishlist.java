package com.jh.productservice.domain.wishlist.entity;

import com.jh.common.domain.timestamp.TimeStamp;
import com.jh.productservice.domain.product.entity.Product;
import com.jh.userservice.domain.entity.Member;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    /**
     * 위시리스트 생성 메서드
     *
     * @param member  위시리스트 소유 회원
     * @param product 위시리스트에 등록할 상품
     * @return Wishlist 객체
     */
    @Builder
    public static Wishlist createWishlist(Member member, Product product) {
        Wishlist wishlist = new Wishlist();
        wishlist.member = member;
        wishlist.product = product;
        return wishlist;
    }
}