package com.dinnervery.backend.dto;

import com.dinnervery.backend.entity.Order;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
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
    @NotEmpty(message = "주문 항목은 최소 1개 이상이어야 합니다")
    private List<OrderItemDto> orderItems;
    
    private BigDecimal totalAmount;
    private BigDecimal discountAmount;
    private BigDecimal finalAmount;
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
                .totalAmount(new BigDecimal(order.getTotalAmount()))
                .discountAmount(new BigDecimal(order.getDiscountAmount()))
                .finalAmount(new BigDecimal(order.getFinalAmount()))
                .createdAt(order.getCreatedAt())
                .modifiedAt(order.getUpdatedAt())
                .build();
    }
}


