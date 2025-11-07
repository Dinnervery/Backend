package com.dinnervery.dto.order.response;

import lombok.Value;

import java.util.List;

@Value
public class DeliveryOrderListResponse {
    List<OrderSummary> orders;
    
    @Value
    public static class OrderSummary {
        Long orderId;
        String status;
        String deliveryTime;
        String address;
        List<OrderItem> orderItems;
        
        @Value
        public static class OrderItem {
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

