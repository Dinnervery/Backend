package com.dinnervery.dto.cart.request;

import jakarta.validation.constraints.Min;
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
public class CartAddItemRequest {
    
    @NotNull(message = "메뉴 ID는 필수입니다.")
    private Long menuId;
    
    @NotNull(message = "수량은 필수입니다.")
    @Min(value = 1, message = "수량은 1 이상이어야 합니다.")
    private Integer menuQuantity;
    
    @NotNull(message = "스타일 ID는 필수입니다.")
    private Long styleId;
    
    private List<OptionRequest> options;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OptionRequest {
        @NotNull(message = "구성 ID는 필수입니다.")
        private Long optionId;
        
        @NotNull(message = "수량은 필수입니다.")
        @Min(value = 1, message = "옵션 수량은 1 이상이어야 합니다.")
        private Integer quantity;
    }
}

