package com.dinnervery.entity;

import com.dinnervery.common.BaseEntity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(name = "address", nullable = false)
    private String address;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status deliveryStatus = Status.REQUESTED;

    @Column(name = "delivery_time", nullable = false)
    private LocalTime deliveryTime;

    @Column(name = "cooking_at")
    private LocalDateTime cookingAt;

    @Column(name = "delivering_at")
    private LocalDateTime deliveringAt;

    @Column(name = "done_at")
    private LocalDateTime doneAt;


    @Column(name = "total_price", nullable = false)
    private int totalPrice;

    @Column(name = "card_number", nullable = false, length = 100)
    private String cardNumber;

    @Builder
    public Order(Customer customer, String address, String cardNumber, LocalTime deliveryTime) {
        this.customer = customer;
        this.address = address;
        this.cardNumber = cardNumber;
        this.deliveryTime = deliveryTime;
        this.totalPrice = 0;
    }

    public void addOrderItem(OrderItem orderItem) {
        this.orderItems.add(orderItem);
        orderItem.setOrder(this);
        orderItem.calculateItemPrice();
        calculateTotalPrice();
    }

    public void removeOrderItem(OrderItem orderItem) {
        this.orderItems.remove(orderItem);
        orderItem.setOrder(null);
        calculateTotalPrice();
    }

    public void calculateTotalPrice() {
        this.totalPrice = this.orderItems.stream()
                .mapToInt(OrderItem::getItemTotalPrice)
                .sum();
    }

    public void startCooking() {
        if (this.deliveryStatus != Status.REQUESTED) {
            throw new IllegalStateException("조리 시작은 REQUESTED 상태에서만 가능합니다. 현재 상태: " + this.deliveryStatus);
        }
        this.deliveryStatus = Status.COOKING;
        this.cookingAt = LocalDateTime.now();
    }

    public void completeCooking() {
        if (this.deliveryStatus != Status.COOKING) {
            throw new IllegalStateException("조리 완료는 COOKING 상태에서만 가능합니다. 현재 상태: " + this.deliveryStatus);
        }
        this.deliveryStatus = Status.COOKED;
    }

    public void startDelivering() {
        if (this.deliveryStatus != Status.COOKED) {
            throw new IllegalStateException("배달 시작은 COOKED 상태에서만 가능합니다. 현재 상태: " + this.deliveryStatus);
        }
        this.deliveryStatus = Status.DELIVERING;
        this.deliveringAt = LocalDateTime.now();
    }

    public void completeDelivering() {
        if (this.deliveryStatus != Status.DELIVERING) {
            throw new IllegalStateException("배달 완료는 DELIVERING 상태에서만 가능합니다. 현재 상태: " + this.deliveryStatus);
        }
        this.deliveryStatus = Status.DONE;
        this.doneAt = LocalDateTime.now();
    }

    public LocalTime getDeliveryTime() {
        return this.deliveryTime;
    }


    public enum Status {
        REQUESTED,  // 주문 요청
        COOKING,    // 조리 중
        COOKED,     // 조리 완료
        DELIVERING, // 배달 중
        DONE        // 배달 완료
    }
}


