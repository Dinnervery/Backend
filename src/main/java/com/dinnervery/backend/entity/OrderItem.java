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
    private ServingStyle servingStyle;

    @Column(name = "ordered_qty", nullable = false)
    private Integer orderedQty;

    @Column(name = "item_price", nullable = false, precision = 8, scale = 0)
    private BigDecimal itemPrice;

    @Column(name = "item_total_price", nullable = false, precision = 8, scale = 0)
    private BigDecimal itemTotalPrice;

    @OneToMany(mappedBy = "orderItem", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItemOption> orderItemOptions = new ArrayList<>();

    @Builder
    public OrderItem(Menu menu, ServingStyle servingStyle, Integer orderedQty) {
        this.menu = menu;
        this.servingStyle = servingStyle;
        this.orderedQty = orderedQty;
        calculateItemPrice();
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public void calculateItemPrice() {
        // 기본 가격 + 서빙 스타일 추가비
        BigDecimal basePrice = menu.getPrice().add(servingStyle.getExtraPrice());
        
        // 옵션 비용 추가
        BigDecimal optionCost = orderItemOptions.stream()
            .map(OrderItemOption::calculateExtraCost)
            .map(BigDecimal::new)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        this.itemPrice = basePrice.add(optionCost);
        this.itemTotalPrice = this.itemPrice.multiply(new BigDecimal(orderedQty));
        
        // Order의 총 금액도 업데이트
        if (this.order != null) {
            this.order.calculateTotalAmount();
        }
    }

    public void updateOrderedQty(Integer newQty) {
        this.orderedQty = newQty;
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


