package com.dinnervery.dto.response;

import lombok.Value;

@Value
public class ErrorResponse {
    String error;
    String message;
}
