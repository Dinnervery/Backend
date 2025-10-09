package com.dinnervery.dto.response;

import lombok.Value;

@Value
public class EmployeeAuthResponse {
    Long employeeId;
    String loginId;
    String name;
    String task;
    String token;
}
