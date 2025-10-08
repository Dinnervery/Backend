package com.dinnervery.dto.order;

import com.dinnervery.entity.ServingStyle;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServingStyleResponse {

    private Long styleId;
    private String name;
    private int unitPrice;

    public static ServingStyleResponse from(ServingStyle servingStyle) {
        return ServingStyleResponse.builder()
                .styleId(servingStyle.getId())
                .name(servingStyle.getName())
                .unitPrice(servingStyle.getExtraPrice())
                .build();
    }
}
