package com.example.racetobuy.domain.page;


import lombok.Getter;

import java.util.List;

@Getter
public class PagedResponseDTO<T> {

    private Long cursor; // 다음 커서
    private int pageSize;    // 요청된 사이즈
    private List<T> data;    // 데이터 리스트
    private boolean hasMore; // 더 많은 데이터 존재 여부

    public PagedResponseDTO(Long cursor, int pageSize, List<T> data,boolean hasMore) {
        this.cursor = cursor;
        this.pageSize = pageSize;
        this.data = data;
        this.hasMore = hasMore;
    }

    // 정적 팩토리 메서드로 생성
    public static <T> PagedResponseDTO<T> of(List<T> data, Long nextCursor, int pageSize, boolean hasMore) {
        return new PagedResponseDTO<>(nextCursor, pageSize, data, hasMore);
    }
}