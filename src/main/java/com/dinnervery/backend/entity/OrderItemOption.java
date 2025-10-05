package com.dinnervery.backend.entity;

import com.dinnervery.backend.common.BaseEntity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "order_item_options")
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
        this.quantity = newQuantity;
        if (this.orderItem != null) {
            this.orderItem.calculateItemPrice();
        }
    }
    
    /**
     * 이 옵션의 추가 비용 계산
     * @return 추가 비용 (양수: 추가, 0: 기본)
     */
    public int calculateExtraCost() {
        return menuOption.calculateExtraCost(quantity);
    }
}
