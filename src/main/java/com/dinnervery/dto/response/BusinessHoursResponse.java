package com.dinnervery.dto.response;

import lombok.Value;

@Value
public class BusinessHoursResponse {
    boolean isOpen;
    String openTime;
    String closeTime;
    String lastOrderTime;
    String message;
}
