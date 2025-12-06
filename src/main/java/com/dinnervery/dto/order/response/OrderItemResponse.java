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
        StyleResponse styleResponse = StyleResponse.builder()
                .styleId(orderItem.getStyleId())
                .name(orderItem.getStyleName())
                .price(orderItem.getStyleExtraPrice())
                .build();
        
        return new OrderItemResponse(
                orderItem.getMenuId(),
                orderItem.getMenuName(),
                orderItem.getQuantity(),
                orderItem.getPrice(),
                orderItem.getItemTotalPrice(),
                styleResponse,
                orderItem.getOrderItemOptions() != null ? 
                        orderItem.getOrderItemOptions().stream()
                                .map(OrderItemOptionResponse::from)
                                .collect(Collectors.toList()) : null
        );
    }
}

