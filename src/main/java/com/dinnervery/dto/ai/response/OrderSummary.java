package com.dinnervery.dto.ai.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderSummary {
    private Long menuId;
    private String menuName;
    private Integer quantity;
    private Long styleId;
    private String styleName;
    private List<OptionInfo> options;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OptionInfo {
        private Long optionId;
        private String optionName;
        private Integer quantity;
    }
}

