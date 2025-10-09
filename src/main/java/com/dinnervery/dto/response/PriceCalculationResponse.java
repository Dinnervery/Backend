package com.dinnervery.dto.response;

import lombok.Value;

@Value
public class PriceCalculationResponse {
    int subtotal;
    Integer discountAmount;
    int finalPrice;
    String customerGrade;
    int discountRate;
}
