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
public class OrderSummaryRequest {
    
    @NotNull(message = "메뉴 ID는 필수입니다.")
    private Long menuId;
    
    @NotNull(message = "스타일 ID는 필수입니다.")
    private Long styleId;
    
    private List<SelectedOption> selectedOptions;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SelectedOption {
        @NotNull(message = "옵션 ID는 필수입니다.")
        private Long optionId;
        
        @NotNull(message = "수량은 필수입니다.")
        private Integer quantity;
    }
}

