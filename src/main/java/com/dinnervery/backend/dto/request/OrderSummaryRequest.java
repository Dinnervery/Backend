package com.dinnervery.backend.dto.request;

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
    
    @NotNull(message = "메뉴 ID는 필수입니다")
    private Long menuId;
    
    @NotNull(message = "선택한 구성품 목록은 필수입니다")
    private List<SelectedOption> selectedOptions;
    
    @NotNull(message = "서빙 스타일 ID는 필수입니다")
    private Long servingStyleId;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SelectedOption {
        @NotNull(message = "구성품 ID는 필수입니다")
        private Long optionId;
        
        @NotNull(message = "수량은 필수입니다")
        private Integer quantity;
    }
}
