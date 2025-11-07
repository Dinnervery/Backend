package com.dinnervery.dto.order.response;

import lombok.Value;

import java.util.List;

@Value
public class OrderListResponse {
    List<OrderSummary> orders;
    
    @Value
    public static class OrderSummary {
        Long orderId;
        String status;
        String deliveryTime;
        List<OrderedItem> orderedItems;
        
        @Value
        public static class OrderedItem {
            Long menuId;
            String name;
            int quantity;
            Long styleId;
            String styleName;
            List<OptionSummaryDto> options;
        }

        @Value
        public static class OptionSummaryDto {
            Long optionId;
            String name;
            int quantity;
        }
    }
}

