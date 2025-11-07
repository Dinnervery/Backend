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
public class Style extends BaseEntity {

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "extra_price", nullable = false)
    private int extraPrice;

    @Builder
    public Style(String name, int extraPrice) {
        this.name = name;
        this.extraPrice = extraPrice;
    }

    public void updateExtraPrice(int newExtraPrice) {
        this.extraPrice = newExtraPrice;
    }
}

