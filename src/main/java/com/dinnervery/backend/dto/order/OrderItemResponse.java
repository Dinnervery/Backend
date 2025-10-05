package com.dinnervery.backend.dto.order;

import com.dinnervery.backend.entity.OrderItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemResponse {

    private Long menuId;
    private String name;
    private int quantity;
    private int unitPrice;
    private int total;
    private ServingStyleResponse servingStyle;
    private List<OrderItemOptionResponse> options;

    public static OrderItemResponse from(OrderItem orderItem) {
        return OrderItemResponse.builder()
                .menuId(orderItem.getMenu().getId())
                .name(orderItem.getMenu().getName())
                .quantity(orderItem.getQuantity())
                .unitPrice(orderItem.getMenu().getPrice())
                .total(orderItem.getItemTotalPrice())
                .servingStyle(orderItem.getServingStyle() != null ? 
                        ServingStyleResponse.from(orderItem.getServingStyle()) : null)
                .options(orderItem.getOrderItemOptions() != null ? 
                        orderItem.getOrderItemOptions().stream()
                                .map(OrderItemOptionResponse::from)
                                .collect(Collectors.toList()) : null)
                .build();
    }
}