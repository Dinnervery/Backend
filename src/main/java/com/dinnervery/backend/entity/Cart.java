package com.dinnervery.backend.entity;

import com.dinnervery.backend.common.BaseEntity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "carts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Cart extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false, unique = true)
    private Customer customer;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> cartItems = new ArrayList<>();

    @Column(name = "total_amount", precision = 8, scale = 0)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Builder
    public Cart(Customer customer) {
        this.customer = customer;
    }

    public void addCartItem(CartItem cartItem) {
        this.cartItems.add(cartItem);
        cartItem.setCart(this);
        calculateTotalAmount();
    }

    public void removeCartItem(CartItem cartItem) {
        this.cartItems.remove(cartItem);
        cartItem.setCart(null);
        calculateTotalAmount();
    }

    public void clearCart() {
        this.cartItems.clear();
        this.totalAmount = BigDecimal.ZERO;
    }

    public void calculateTotalAmount() {
        this.totalAmount = this.cartItems.stream()
                .map(CartItem::getItemTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}


