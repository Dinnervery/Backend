package com.dinnervery.dto.order;

import com.dinnervery.entity.Order;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Value
public class OrderResponse {

    Long orderId;
    Long customerId;
    String status;
    int totalPrice;
    List<OrderItemResponse> orderItems;
    LocalDateTime createdAt;
    String deliveryTime;

    public static OrderResponse from(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getCustomer().getId(),
                order.getDeliveryStatus().name(),
                order.getTotalPrice(),
                order.getOrderItems().stream()
                        .map(OrderItemResponse::from)
                        .collect(Collectors.toList()),
                order.getCreatedAt(),
                order.getDeliveryTime().toString()
        );
    }
}
