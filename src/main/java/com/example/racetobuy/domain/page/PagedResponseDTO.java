package com.example.racetobuy.domain.page;


import lombok.Getter;

import java.util.List;

@Getter
public class PagedResponseDTO<T> {

    private Long nextCursor; // 다음 커서
    private int pageSize;    // 요청된 사이즈
    private List<T> data;    // 데이터 리스트

    public PagedResponseDTO(Long nextCursor, int pageSize, List<T> data) {
        this.nextCursor = nextCursor;
        this.pageSize = pageSize;
        this.data = data;
    }




}