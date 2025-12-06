package com.dinnervery.dto.auth.response;

import lombok.Value;

@Value
public class UnifiedLoginResponse {
    Long userId;
    String loginId;
    String name;
    String role;  // "CUSTOMER", "COOK", "DELIVERY"
    String token;
    // 고객인 경우
    String grade;  // "BASIC", "VIP" (고객만, null 가능)
    // 직원인 경우
    String task;   // "COOK", "DELIVERY" (직원만, null 가능)
}

