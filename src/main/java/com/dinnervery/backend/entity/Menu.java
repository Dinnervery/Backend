package com.dinnervery.backend.entity;

import com.dinnervery.backend.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "menus")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Menu extends BaseEntity {

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "price", nullable = false, precision = 8, scale = 0)
    private BigDecimal price;



    @Builder
    public Menu(String name, String description, BigDecimal price) {
        this.name = name;
        this.description = description;
        this.price = price;
    }


}
