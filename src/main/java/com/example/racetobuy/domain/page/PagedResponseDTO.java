package com.example.racetobuy.domain.page;


import lombok.Getter;

import java.util.List;

@Getter
public class PagedResponseDTO<T> {

    private Long cursor; // 다음 커서
    private int pageSize;    // 요청된 사이즈
    private List<T> data;    // 데이터 리스트

    public PagedResponseDTO(Long cursor, int pageSize, List<T> data) {
        this.cursor = cursor;
        this.pageSize = pageSize;
        this.data = data;
    }
}