package com.jh.common.domain.timestamp;


import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@MappedSuperclass
public abstract class TimeStamp {

    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;

    public TimeStamp() {
        // 생성 시 현재 시간으로 초기화
        this.createdAt = LocalDateTime.now();
        this.modifiedAt = LocalDateTime.now();
    }

    // 생성 시 현재 시간으로 초기화
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.modifiedAt = LocalDateTime.now();
    }

    // 수정 시 수정 시간을 갱신
    @PreUpdate
    public void preUpdate() {
        this.modifiedAt = LocalDateTime.now();
    }

    // 생성 시간을 반환
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // 수정 시간을 반환
    public LocalDateTime getModifiedAt() {
        return modifiedAt;
    }

    // 수정 시간을 갱신
    public void updateModifiedAt() {
        this.modifiedAt = LocalDateTime.now();
    }
}