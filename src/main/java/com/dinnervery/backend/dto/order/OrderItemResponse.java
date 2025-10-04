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

    private Long orderItemId;
    private Long menuId;
    private String menuName;
    private int orderedQty;
    private int itemTotalPrice;
    private List<OrderItemOptionResponse> options;

    public static OrderItemResponse from(OrderItem orderItem) {
        return OrderItemResponse.builder()
                .orderItemId(orderItem.getId())
                .menuId(orderItem.getMenu().getId())
                .menuName(orderItem.getMenu().getName())
                .orderedQty(orderItem.getOrderedQty())
                .itemTotalPrice(orderItem.getItemTotalPrice())
                .options(orderItem.getOrderItemOptions() != null ? 
                        orderItem.getOrderItemOptions().stream()
                                .map(OrderItemOptionResponse::from)
                                .collect(Collectors.toList()) : null)
                .build();
    }
}