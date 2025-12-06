package com.dinnervery.dto.ai.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiOrderResponse {
    private Boolean success;
    private AiChatResponse result;
    private String error;
}

