package com.dinnervery.dto.order.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreateRequest {

    @NotNull(message = "고객 ID는 필수입니다.")
    private Long customerId;

    @NotNull(message = "주소는 필수입니다.")
    private String address;

    @NotNull(message = "카드 번호는 필수입니다.")
    private String cardNumber;

    @NotNull(message = "배송 시간은 필수입니다.")
    private LocalTime deliveryTime;
}

