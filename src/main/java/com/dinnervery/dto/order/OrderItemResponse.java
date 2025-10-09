package com.dinnervery.dto.order;

import com.dinnervery.entity.OrderItem;
import lombok.Value;

import java.util.List;
import java.util.stream.Collectors;

@Value
public class OrderItemResponse {

    Long menuId;
    String name;
    int quantity;
    int unitPrice;
    int total;
    ServingStyleResponse servingStyle;
    List<OrderItemOptionResponse> options;

    public static OrderItemResponse from(OrderItem orderItem) {
        return new OrderItemResponse(
                orderItem.getMenu().getId(),
                orderItem.getMenu().getName(),
                orderItem.getQuantity(),
                orderItem.getMenu().getPrice(),
                orderItem.getItemTotalPrice(),
                orderItem.getServingStyle() != null ? 
                        ServingStyleResponse.from(orderItem.getServingStyle()) : null,
                orderItem.getOrderItemOptions() != null ? 
                        orderItem.getOrderItemOptions().stream()
                                .map(OrderItemOptionResponse::from)
                                .collect(Collectors.toList()) : null
        );
    }
}
