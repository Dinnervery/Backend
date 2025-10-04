package com.dinnervery.backend.dto;

import com.dinnervery.backend.entity.OrderItem;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemDto {

    private Long id;
    private Long menuId;
    private String menuName;
    
    @NotNull(message = "주문 수량은 필수입니다")
    @Min(value = 1, message = "주문 수량은 1 이상이어야 합니다")
    private Integer orderedQty;
    
    private int itemPrice;
    private int itemTotalPrice;

    public static OrderItemDto from(OrderItem orderItem) {
        return OrderItemDto.builder()
                .id(orderItem.getId())
                .menuId(orderItem.getMenu().getId())
                .menuName(orderItem.getMenu().getName())
                .orderedQty(orderItem.getOrderedQty())
                .itemPrice(orderItem.getItemPrice())
                .itemTotalPrice(orderItem.getItemTotalPrice())
                .build();
    }
}


