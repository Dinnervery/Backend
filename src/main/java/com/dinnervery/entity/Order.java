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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "address_id", nullable = false)
    private Address address;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

    @Column(name = "order_date", nullable = false)
    private LocalDateTime orderDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status deliveryStatus = Status.REQUESTED;

    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedAt;

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

    @Column(name = "discount_amount")
    private Integer discountAmount;

    @Column(name = "final_price")
    private int finalPrice;

    @Column(name = "card_number", nullable = false, length = 100)
    private String cardNumber;

    @Builder
    public Order(Customer customer, Address address, String cardNumber, LocalTime deliveryTime) {
        this.customer = customer;
        this.address = address;
        this.cardNumber = cardNumber;
        this.deliveryTime = deliveryTime;
        this.orderDate = LocalDateTime.now();
        this.requestedAt = LocalDateTime.now();
        this.totalPrice = 0;
        this.finalPrice = 0;
    }

    public void addOrderItem(OrderItem orderItem) {
        this.orderItems.add(orderItem);
        orderItem.setOrder(this);
        orderItem.calculateItemPrice(); // OrderItem의 가격 계산
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
        this.finalPrice = this.totalPrice - (this.discountAmount != null ? this.discountAmount : 0);
    }

    public void setDiscountAmount(int discountAmount) {
        this.discountAmount = discountAmount;
        this.finalPrice = this.totalPrice - this.discountAmount;
    }

    // 배달 상태 변경 메서드들
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

    public void completeDelivery() {
        if (this.deliveryStatus != Status.DELIVERING) {
            throw new IllegalStateException("배달 완료는 DELIVERING 상태에서만 가능합니다. 현재 상태: " + this.deliveryStatus);
        }
        this.deliveryStatus = Status.DONE;
        this.doneAt = LocalDateTime.now();
    }


    // 배송 희망 시간 반환
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


