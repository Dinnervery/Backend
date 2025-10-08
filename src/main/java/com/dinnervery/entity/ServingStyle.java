package com.dinnervery.entity;

import com.dinnervery.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "serving_styles")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ServingStyle extends BaseEntity {

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "extra_price", nullable = false)
    private int extraPrice;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Builder
    public ServingStyle(String name, int extraPrice, boolean isActive) {
        this.name = name;
        this.extraPrice = extraPrice;
        this.isActive = isActive;
    }

    public void updateExtraPrice(int newExtraPrice) {
        this.extraPrice = newExtraPrice;
    }

    public void deactivate() { 
        this.isActive = false; 
    }
    
    public void activate() { 
        this.isActive = true; 
    }
}
