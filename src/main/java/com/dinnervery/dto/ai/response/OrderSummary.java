package com.dinnervery.dto.ai.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderSummary {
    private Long menuId;
    private String menuName;
    private Integer quantity;
}

