package com.dinnervery.dto.order.response;

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
    private int defaultQty;
    private int price;
    private int extraPrice;

    public static OrderItemOptionResponse from(OrderItemOption orderItemOption) {
        return OrderItemOptionResponse.builder()
                .optionId(orderItemOption.getMenuOption().getId())
                .name(orderItemOption.getMenuOption().getName())
                .quantity(orderItemOption.getQuantity())
                .defaultQty(orderItemOption.getMenuOption().getDefaultQty())
                .price(orderItemOption.getMenuOption().getPrice())
                .extraPrice(orderItemOption.calculateExtraCost())
                .build();
    }
}

