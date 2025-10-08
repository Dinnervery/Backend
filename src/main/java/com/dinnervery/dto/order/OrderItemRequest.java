package com.dinnervery.dto.order;

import jakarta.validation.Valid;
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
public class OrderItemRequest {

    @NotNull(message = "메뉴 ID는 필수입니다.")
    private Long menuId;

    @NotNull(message = "서빙 스타일 ID는 필수입니다.")
    private Long servingStyleId;

    @Min(value = 1, message = "수량은 1 이상이어야 합니다.")
    private int quantity;

    @Valid
    private List<OrderItemOptionRequest> options;
}
