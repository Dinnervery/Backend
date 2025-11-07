package com.dinnervery.dto.order.response;

import com.dinnervery.entity.Style;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StyleResponse {

    private Long styleId;
    private String name;
    private int price;

    public static StyleResponse from(Style style) {
        return StyleResponse.builder()
                .styleId(style.getId())
                .name(style.getName())
                .price(style.getExtraPrice())
                .build();
    }
}

