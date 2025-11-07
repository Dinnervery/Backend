package com.dinnervery.dto.order.response;

import lombok.Value;

@Value
public class PriceCalculationResponse {
    int subtotal;
    String customerGrade;
}

