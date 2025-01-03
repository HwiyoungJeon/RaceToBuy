package com.jh.productservice.domain.wishlist.repository;

import com.jh.productservice.domain.wishlist.entity.Wishlist;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, Long> {
    /**
     * 특정 회원과 상품 ID로 위시리스트 항목을 조회
     *
     * @param memberId  회원 ID
     * @param productId 상품 ID
     * @return 해당 위시리스트 항목 (Optional)
     */
    Optional<Wishlist> findByMemberMemberIdAndProductProductId(Long memberId, Long productId);

    /**
     * 특정 회원의 위시리스트를 정렬하여 조회
     *
     * @param memberId 회원 ID
     * @param sort     정렬 조건
     * @return 위시리스트 목록
     */
    List<Wishlist> findAllByMemberMemberId(Long memberId, Sort sort);

    /**
     * 특정 회원과 상품이 이미 위시리스트에 존재하는지 확인
     *
     * @param memberId  회원 ID
     * @param productId 상품 ID
     * @return 존재 여부 (boolean)
     */
    boolean existsByMemberMemberIdAndProductProductId(Long memberId, Long productId);

    /**
     * 특정 회원의 모든 위시리스트 항목 삭제
     *
     * @param memberId 회원 ID
     */
    void deleteAllByMember_MemberId(Long memberId);
}