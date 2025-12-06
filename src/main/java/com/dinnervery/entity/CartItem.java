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

    @Column(name = "menu_id", nullable = false)
    private Long menuId;

    @Column(name = "menu_name", nullable = false)
    private String menuName;

    @Column(name = "menu_price", nullable = false)
    private int menuPrice;

    @Column(name = "style_id", nullable = false)
    private Long styleId;

    @Column(name = "style_name", nullable = false)
    private String styleName;

    @Column(name = "style_extra_price", nullable = false)
    private int styleExtraPrice;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "item_price")
    private int price;

    @Column(name = "item_total_price")
    private int itemTotalPrice;

    @OneToMany(mappedBy = "cartItem", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<CartItemOption> cartItemOptions = new java.util.ArrayList<>();

    @Builder
    public CartItem(Long menuId, String menuName, int menuPrice, Long styleId, String styleName, int styleExtraPrice, Integer quantity) {
        if (quantity != null && quantity < 1) {
            throw new IllegalArgumentException("수량은 1 이상이어야 합니다.");
        }
        this.menuId = menuId;
        this.menuName = menuName;
        this.menuPrice = menuPrice;
        this.styleId = styleId;
        this.styleName = styleName;
        this.styleExtraPrice = styleExtraPrice;
        this.quantity = quantity;
        calculateItemPrice();
    }

    public void setCart(Cart cart) {
        this.cart = cart;
    }

    public void setStyle(Long styleId, String styleName, int styleExtraPrice) {
        this.styleId = styleId;
        this.styleName = styleName;
        this.styleExtraPrice = styleExtraPrice;
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
        int basePrice = menuPrice + styleExtraPrice;
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
