package com.dinnervery.entity;

import com.dinnervery.common.BaseEntity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "order_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_id", nullable = false)
    private Menu menu;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "serving_style_id", nullable = false)
    private Style style;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "item_price", nullable = false)
    private int price;

    @Column(name = "item_total_price", nullable = false)
    private int itemTotalPrice;

    @OneToMany(mappedBy = "orderItem", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItemOption> orderItemOptions = new ArrayList<>();

    @Builder
    public OrderItem(Menu menu, Style style, Integer quantity) {
        if (quantity != null && quantity < 1) {
            throw new IllegalArgumentException("수량은 1 이상이어야 합니다.");
        }
        this.menu = menu;
        this.style = style;
        this.quantity = quantity;
        // calculateItemPrice()는 addOrderItem에서 호출됨
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public void calculateItemPrice() {
        if (menu == null || style == null) {
            return; // menu나 style이 null이면 계산하지 않음
        }
        
        // 기본 가격 + 스타일 추가 가격
        int basePrice = menu.getPrice() + style.getExtraPrice();
        
        // 옵션 비용 추가
        int optionCost = orderItemOptions.stream()
            .mapToInt(OrderItemOption::calculateExtraCost)
            .sum();
        
        this.price = basePrice + optionCost;
        this.itemTotalPrice = this.price * quantity;
        
        // Order의 총금액 업데이트
        if (this.order != null) {
            this.order.calculateTotalPrice();
        }
    }

    public void updateQuantity(Integer newQty) {
        if (newQty == null || newQty < 1) {
            throw new IllegalArgumentException("수량은 1 이상이어야 합니다.");
        }
        this.quantity = newQty;
        calculateItemPrice();
    }

    public void addOrderItemOption(OrderItemOption orderItemOption) {
        this.orderItemOptions.add(orderItemOption);
        orderItemOption.setOrderItem(this);
        calculateItemPrice();
    }

    public void removeOrderItemOption(OrderItemOption orderItemOption) {
        this.orderItemOptions.remove(orderItemOption);
        orderItemOption.setOrderItem(null);
        calculateItemPrice();
    }
}


