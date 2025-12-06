package com.dinnervery.entity;

import com.dinnervery.common.BaseEntity;

import jakarta.persistence.*;
import org.hibernate.annotations.BatchSize;
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

    @Column(name = "item_price", nullable = false)
    private int price;

    @Column(name = "item_total_price", nullable = false)
    private int itemTotalPrice;

    @OneToMany(mappedBy = "orderItem", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 50)
    private List<OrderItemOption> orderItemOptions = new ArrayList<>();

    @Builder
    public OrderItem(Long menuId, String menuName, int menuPrice, Long styleId, String styleName, int styleExtraPrice, Integer quantity) {
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
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public void calculateItemPrice() {
        int basePrice = menuPrice + styleExtraPrice;
        
        int optionCost = orderItemOptions.stream()
            .mapToInt(OrderItemOption::calculateExtraCost)
            .sum();
        
        this.price = basePrice + optionCost;
        this.itemTotalPrice = this.price * quantity;
        
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


