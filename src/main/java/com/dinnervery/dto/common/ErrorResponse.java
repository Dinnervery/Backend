package com.dinnervery.dto.common;

import lombok.Value;

@Value
public class ErrorResponse {
    String error;
    String message;
}

