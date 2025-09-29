package com.dinnervery.backend.dto.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreateRequest {

    @NotNull(message = "고객 ID는 필수입니다")
    private Long customerId;

    @NotNull(message = "주소 ID는 필수입니다")
    private Long addressId;

    @Pattern(regexp = "^[0-9]{16}$", message = "올바른 카드번호 형식이 아닙니다 (16자리 숫자)")
    private String cardNumber;

    @Valid
    @NotEmpty(message = "주문 항목은 최소 1개 이상이어야 합니다")
    private List<OrderItemRequest> items;
}
