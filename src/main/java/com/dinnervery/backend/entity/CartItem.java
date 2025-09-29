package com.dinnervery.backend.entity;

import com.dinnervery.backend.common.BaseEntity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "cart_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CartItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_id", nullable = false)
    private Menu menu;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "serving_style_id", nullable = false)
    private ServingStyle servingStyle;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "item_price", precision = 8, scale = 0)
    private BigDecimal itemPrice;

    @Column(name = "item_total_price", precision = 8, scale = 0)
    private BigDecimal itemTotalPrice;

    @Builder
    public CartItem(Menu menu, ServingStyle servingStyle, Integer quantity) {
        this.menu = menu;
        this.servingStyle = servingStyle;
        this.quantity = quantity;
        calculateItemPrice();
    }

    public void setCart(Cart cart) {
        this.cart = cart;
    }

    public void updateQuantity(Integer newQuantity) {
        this.quantity = newQuantity;
        calculateItemPrice();
    }

    private void calculateItemPrice() {
        // 기본 가격 + 서빙 스타일 추가비
        BigDecimal basePrice = menu.getPrice().add(servingStyle.getExtraPrice());
        
        this.itemPrice = basePrice;
        this.itemTotalPrice = basePrice.multiply(new BigDecimal(quantity));
    }
}
