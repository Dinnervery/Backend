package com.dinnervery.dto.auth.response;

import lombok.Value;

@Value
public class LoginResponse {
    Long customerId;
    String loginId;
    String name;
    String grade;
    String token;
}

