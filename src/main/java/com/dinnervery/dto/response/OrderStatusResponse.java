package com.dinnervery.dto.response;

import lombok.Value;

import java.time.LocalDateTime;
import java.util.List;

@Value
public class OrderStatusResponse {
    Long orderId;
    String status;
    String deliveryTime;
    List<OrderItemDetail> orderItems;
    LocalDateTime createdAt;
    LocalDateTime deliveredAt;
    
    @Value
    public static class OrderItemDetail {
        Long menuId;
        String menuName;
        int menuQuantity;
        int menuPrice;
        Long styleId;
        String styleName;
        int stylePrice;
        List<OptionDetail> options;
        
        @Value
        public static class OptionDetail {
            Long optionId;
            String optionName;
            int optionQuantity;
            int optionPrice;
        }
    }
}
