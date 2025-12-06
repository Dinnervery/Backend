package com.dinnervery.dto.ai.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiChatResponse {
    private String reply;
    private String state;
    
    @JsonProperty("order_summary")
    private String orderSummary;
}

