package com.dinnervery.dto.response;

import lombok.Value;

@Value
public class CustomerResponse {
    Long customerId;
    String loginId;
    String name;
    String phoneNumber;
    String grade;
    Integer orderCount;
}
