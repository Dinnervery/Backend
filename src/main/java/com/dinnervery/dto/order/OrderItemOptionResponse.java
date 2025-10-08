package com.dinnervery.dto.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.dinnervery.entity.OrderItemOption;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemOptionResponse {

    private Long optionId;
    private String name;
    private int quantity;
    private int unitPrice;
    private int total;

    public static OrderItemOptionResponse from(OrderItemOption orderItemOption) {
        return OrderItemOptionResponse.builder()
                .optionId(orderItemOption.getMenuOption().getId())
                .name(orderItemOption.getMenuOption().getItemName())
                .quantity(orderItemOption.getQuantity())
                .unitPrice(orderItemOption.getMenuOption().getItemPrice())
                .total(orderItemOption.calculateExtraCost())
                .build();
    }
}
