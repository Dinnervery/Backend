package com.dinnervery.backend.dto.order;

import com.dinnervery.backend.entity.Order;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {

    private Long orderId;
    private Long customerId;
    private String customerName;
    private List<OrderItemResponse> orderItems;
    private int totalAmount;
    private int discountAmount;
    private int finalAmount;
    private String status;
    private LocalDateTime orderDate;
    private LocalDateTime requestedAt;
    private LocalDateTime cookingAt;
    private LocalDateTime handoverAt;
    private LocalDateTime deliveringAt;
    private LocalDateTime doneAt;
    private LocalDateTime canceledAt;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;

    public static OrderResponse from(Order order) {
        return OrderResponse.builder()
                .orderId(order.getId())
                .customerId(order.getCustomer().getId())
                .customerName(order.getCustomer().getName())
                .orderItems(order.getOrderItems().stream()
                        .map(OrderItemResponse::from)
                        .collect(Collectors.toList()))
                .totalAmount(order.getTotalAmount())
                .discountAmount(order.getDiscountAmount())
                .finalAmount(order.getFinalAmount())
                .status(order.getDeliveryStatus().name())
                .orderDate(order.getOrderDate())
                .requestedAt(order.getRequestedAt())
                .cookingAt(order.getCookingAt())
                .deliveringAt(order.getDeliveringAt())
                .doneAt(order.getDoneAt())
                .canceledAt(order.getCanceledAt())
                .createdAt(order.getCreatedAt())
                .modifiedAt(order.getUpdatedAt())
                .build();
    }
}
