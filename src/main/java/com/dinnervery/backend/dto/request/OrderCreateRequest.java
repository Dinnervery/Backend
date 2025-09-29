package com.dinnervery.backend.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
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
public class OrderCreateRequest {

    @NotNull(message = "고객 ID는 필수입니다")
    private Long customerId;

    @NotNull(message = "주소 ID는 필수입니다")
    private Long addressId;

    @NotNull(message = "카드 번호는 필수입니다")
    private String cardNumber;

    @Valid
    @NotEmpty(message = "주문 항목은 최소 1개 이상이어야 합니다")
    private List<OrderItemCreateRequest> orderItems;
}


