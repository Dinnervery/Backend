package com.dinnervery.dto.order.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderPreviewRequest {
    
    @NotNull(message = "고객 ID는 필수입니다.")
    private Long customerId;
}

