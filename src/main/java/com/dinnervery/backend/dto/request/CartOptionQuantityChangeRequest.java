package com.dinnervery.backend.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartOptionQuantityChangeRequest {
    
    @NotNull(message = "수량 변경값은 필수입니다")
    private Integer quantityChange;
}
