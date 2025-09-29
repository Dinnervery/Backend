package com.dinnervery.backend.entity;

import com.dinnervery.backend.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "grades")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Grade extends BaseEntity {

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "discount_rate", nullable = false, precision = 3, scale = 0)
    private BigDecimal discountRate = BigDecimal.ZERO;

    @Column(name = "min_order_count", nullable = false)
    private Integer minOrderCount = 0;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Builder
    public Grade(String name, String description, BigDecimal discountRate, Integer minOrderCount) {
        this.name = name;
        this.description = description;
        this.discountRate = discountRate;
        this.minOrderCount = minOrderCount;
    }

    public void updateDiscountRate(BigDecimal newDiscountRate) {
        this.discountRate = newDiscountRate;
    }

    public void updateStatus(boolean isActive) {
        this.isActive = isActive;
    }
}


