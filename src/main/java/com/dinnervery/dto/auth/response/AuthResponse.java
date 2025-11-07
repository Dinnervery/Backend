package com.dinnervery.dto.auth.response;

import lombok.Value;

@Value
public class AuthResponse {
    Long customerId;
    String loginId;
    String name;
    String phoneNumber;
    String address;
    String grade;
    String token;
}

