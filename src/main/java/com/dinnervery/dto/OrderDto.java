package com.dinnervery.dto;

import com.dinnervery.entity.Order;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDto {

    private Long id;
    private Long customerId;
    private String customerName;
    
    @Valid
    @NotEmpty(message = "주문 항목은 최소 1개 이상이어야 합니다.")
    private List<OrderItemDto> orderItems;
    
    private int totalPrice;
    private int discountAmount;
    private int finalPrice;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;

    public static OrderDto from(Order order) {
        return OrderDto.builder()
                .id(order.getId())
                .customerId(order.getCustomer().getId())
                .customerName(order.getCustomer().getName())
                .orderItems(order.getOrderItems().stream()
                        .map(OrderItemDto::from)
                        .toList())
                .totalPrice(order.getTotalPrice())
                .discountAmount(order.getDiscountAmount() != null ? order.getDiscountAmount() : 0)
                .finalPrice(order.getFinalPrice())
                .createdAt(order.getCreatedAt())
                .modifiedAt(order.getUpdatedAt())
                .build();
    }
}


