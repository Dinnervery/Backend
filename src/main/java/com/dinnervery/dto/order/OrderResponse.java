package com.dinnervery.dto.order;

import com.dinnervery.entity.Order;
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
    private String status;
    private int totalPrice;
    private List<OrderItemResponse> orderItems;
    private LocalDateTime createdAt;
    private String deliveryTime;

    public static OrderResponse from(Order order) {
        return OrderResponse.builder()
                .orderId(order.getId())
                .customerId(order.getCustomer().getId())
                .status(order.getDeliveryStatus().name())
                .totalPrice(order.getTotalPrice())
                .orderItems(order.getOrderItems().stream()
                        .map(OrderItemResponse::from)
                        .collect(Collectors.toList()))
                .createdAt(order.getCreatedAt())
                .deliveryTime(order.getDeliveryTime().toString())
                .build();
    }
}
