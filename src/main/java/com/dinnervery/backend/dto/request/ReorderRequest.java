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
public class ReorderRequest {
    
    @NotNull(message = "고객 ID는 필수입니다")
    private Long customerId;
    
    @NotNull(message = "주소 ID는 필수입니다")
    private Long addressId;
    
    @NotNull(message = "카드 번호는 필수입니다")
    private String cardNumber;
}
