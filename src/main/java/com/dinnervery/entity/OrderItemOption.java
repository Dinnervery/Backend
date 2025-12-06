package com.dinnervery.entity;

import com.dinnervery.common.BaseEntity;

import jakarta.persistence.*;
import org.hibernate.annotations.BatchSize;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "order_item_options")
@BatchSize(size = 50)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItemOption extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id", nullable = false)
    private OrderItem orderItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_option_id", nullable = false)
    private MenuOption menuOption;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Builder
    public OrderItemOption(MenuOption menuOption, Integer quantity) {
        if (quantity != null && quantity < 1) {
            throw new IllegalArgumentException("수량은 1 이상이어야 합니다.");
        }
        this.menuOption = menuOption;
        this.quantity = quantity;
    }

    public void setOrderItem(OrderItem orderItem) {
        this.orderItem = orderItem;
        if (orderItem != null && !orderItem.getOrderItemOptions().contains(this)) {
            orderItem.addOrderItemOption(this);
        }
    }

    public void updateQuantity(Integer newQuantity) {
        if (newQuantity == null || newQuantity < 1) {
            throw new IllegalArgumentException("수량은 1 이상이어야 합니다.");
        }
        this.quantity = newQuantity;
        if (this.orderItem != null) {
            this.orderItem.calculateItemPrice();
        }
    }
    
    public int calculateExtraCost() {
        return menuOption.calculateExtraCost(quantity);
    }
}
