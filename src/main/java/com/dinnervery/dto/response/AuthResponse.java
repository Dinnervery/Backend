package com.dinnervery.dto.response;

import lombok.Value;

@Value
public class AuthResponse {
    Long customerId;
    String loginId;
    String name;
    String phoneNumber;
    String grade;
    String token;
}
