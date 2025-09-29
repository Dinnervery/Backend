package com.dinnervery.backend.entity;

import com.dinnervery.backend.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "serving_styles")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ServingStyle extends BaseEntity {

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "extra_price", nullable = false, precision = 5, scale = 0)
    private BigDecimal extraPrice;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Builder
    public ServingStyle(String name, BigDecimal extraPrice, boolean isActive) {
        this.name = name;
        this.extraPrice = extraPrice;
        this.isActive = isActive;
    }

    public void updateExtraPrice(BigDecimal newExtraPrice) {
        this.extraPrice = newExtraPrice;
    }

    public void deactivate() { 
        this.isActive = false; 
    }
    
    public void activate() { 
        this.isActive = true; 
    }
}
