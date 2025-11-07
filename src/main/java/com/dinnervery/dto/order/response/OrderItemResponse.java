package com.dinnervery.dto.order.response;

import com.dinnervery.entity.OrderItem;
import lombok.Value;

import java.util.List;
import java.util.stream.Collectors;

@Value
public class OrderItemResponse {

    Long menuId;
    String name;
    int quantity;
    int price;
    int subTotal;
    StyleResponse style;
    List<OrderItemOptionResponse> options;

    public static OrderItemResponse from(OrderItem orderItem) {
        return new OrderItemResponse(
                orderItem.getMenu().getId(),
                orderItem.getMenu().getName(),
                orderItem.getQuantity(),
                orderItem.getPrice(),
                orderItem.getItemTotalPrice(),
                orderItem.getStyle() != null ? 
                        StyleResponse.from(orderItem.getStyle()) : null,
                orderItem.getOrderItemOptions() != null ? 
                        orderItem.getOrderItemOptions().stream()
                                .map(OrderItemOptionResponse::from)
                                .collect(Collectors.toList()) : null
        );
    }
}

