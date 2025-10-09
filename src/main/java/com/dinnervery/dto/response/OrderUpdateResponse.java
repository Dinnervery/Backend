package com.dinnervery.dto.response;

import lombok.Value;

import java.time.LocalDateTime;

@Value
public class OrderUpdateResponse {
    Long orderId;
    String status;
    LocalDateTime updatedAt;
}
