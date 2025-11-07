package com.dinnervery.dto.order.response;

import lombok.Value;

@Value
public class OrderUpdateResponse {
    Long orderId;
    String status;
}

