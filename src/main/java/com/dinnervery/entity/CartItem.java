package com.dinnervery.entity;

import com.dinnervery.common.BaseEntity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
    private Style style;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "item_price")
    private int price;

    @Column(name = "item_total_price")
    private int itemTotalPrice;

    @OneToMany(mappedBy = "cartItem", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<CartItemOption> cartItemOptions = new java.util.ArrayList<>();

    @Builder
    public CartItem(Menu menu, Style style, Integer quantity) {
        if (quantity != null && quantity < 1) {
            throw new IllegalArgumentException("수량은 1 이상이어야 합니다.");
        }
        this.menu = menu;
        this.style = style;
        this.quantity = quantity;
        calculateItemPrice();
    }

    public void setCart(Cart cart) {
        this.cart = cart;
    }

    public void setStyle(Style style) {
        this.style = style;
        calculateItemPrice();
    }

    public void updateQuantity(Integer newQuantity) {
        if (newQuantity == null || newQuantity < 1) {
            throw new IllegalArgumentException("수량은 1 이상이어야 합니다.");
        }
        this.quantity = newQuantity;
        calculateItemPrice();
    }

    void calculateItemPrice() {
        int basePrice = menu.getPrice() + style.getExtraPrice();
        int optionsExtra = 0;
        for (CartItemOption option : cartItemOptions) {
            optionsExtra += option.calculateExtraCost();
        }
        int unitPrice = basePrice + optionsExtra;
        this.price = unitPrice;
        this.itemTotalPrice = unitPrice * quantity;
    }

    public java.util.List<CartItemOption> getCartItemOptions() {
        return cartItemOptions;
    }

    public void addCartItemOption(CartItemOption option) {
        this.cartItemOptions.add(option);
        option.setCartItem(this);
        calculateItemPrice();
    }

    public void removeCartItemOption(CartItemOption option) {
        this.cartItemOptions.remove(option);
        option.setCartItem(null);
        calculateItemPrice();
    }
}
