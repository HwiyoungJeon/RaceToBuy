package com.jh.productservice.domain.product.entity;

import com.jh.common.constant.ErrorCode;
import com.jh.common.domain.timestamp.TimeStamp;
import com.jh.common.exception.BusinessException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Table(name = "product")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product extends TimeStamp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long productId;

    @Column(name = "product_name", nullable = false, length = 100)
    private String productName;

    @Column(name = "product_description", columnDefinition = "TEXT")
    private String productDescription;

    @Column(name = "price", nullable = false, columnDefinition = "DECIMAL(10,2)")
    private BigDecimal price;

    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity;

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EventProduct> eventProducts = new ArrayList<>();

    public void updateStockQuantity(int quantity) {
        if (quantity < 0) {
            throw new BusinessException(ErrorCode.INVALID_QUANTITY);
        }
        this.stockQuantity = quantity;
    }

    public void reduceStock(int amount) {
        if (this.stockQuantity < amount) {
            throw new BusinessException(ErrorCode.STOCK_NOT_ENOUGH);
        }
        this.stockQuantity -= amount;
    }

    public void increaseStock(int amount) {
        this.stockQuantity += amount;
    }

}
