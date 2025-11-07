package com.dinnervery.dto.order.response;

import lombok.Value;

import java.util.List;

@Value
public class OrderPreviewResponse {
    List<ItemResponse> items;
    Integer itemsTotalPrice;
    
    @Value
    public static class ItemResponse {
        String menuName;
        Integer quantity;
        Integer price;
        String styleName;
        List<OptionResponse> options;
    }
    
    @Value
    public static class OptionResponse {
        String optionName;
        Integer quantity;
    }
}

