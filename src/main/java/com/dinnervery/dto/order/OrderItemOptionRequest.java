package com.dinnervery.dto.order;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemOptionRequest {

    @NotNull(message = "메뉴 옵션 ID는 필수입니다.")
    private Long menuOptionId;

    @Min(value = 0, message = "수량은 0 이상이어야 합니다.")
    private int quantity;
}
