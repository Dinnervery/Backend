package com.dinnervery.dto.order.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceCalculationRequest {
    
    @NotNull(message = "고객 ID는 필수입니다.")
    private Long customerId;
    
    @NotNull(message = "주문 목록은 필수입니다.")
    private List<OrderItemRequest> items;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemRequest {
        @NotNull(message = "메뉴 ID는 필수입니다.")
        private Long menuId;
        
        @NotNull(message = "수량은 필수입니다.")
        private Integer quantity;
        
        @NotNull(message = "스타일 ID는 필수입니다.")
        private Long styleId;
        
        private List<Long> optionIds;
    }
}

