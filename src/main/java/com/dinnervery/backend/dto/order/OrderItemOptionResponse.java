package com.dinnervery.backend.dto.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

import com.dinnervery.backend.entity.OrderItemOption;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemOptionResponse {

    private Long orderItemOptionId;
    private Long menuOptionId;
    private String menuOptionName;
    private int orderedQty;
    private BigDecimal optionPrice;

    public static OrderItemOptionResponse from(OrderItemOption orderItemOption) {
        return OrderItemOptionResponse.builder()
                .orderItemOptionId(orderItemOption.getId())
                .menuOptionId(orderItemOption.getMenuOption().getId())
                .menuOptionName(orderItemOption.getMenuOption().getItemName())
                .orderedQty(orderItemOption.getOrderedQty())
                .optionPrice(new BigDecimal(orderItemOption.calculateExtraCost()))
                .build();
    }
}