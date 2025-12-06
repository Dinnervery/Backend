package com.dinnervery.controller;

import com.dinnervery.dto.order.response.OrderListResponse;
import com.dinnervery.dto.order.response.DeliveryOrderListResponse;
import com.dinnervery.dto.order.response.OrderUpdateResponse;
import com.dinnervery.security.SecurityUtils;
import com.dinnervery.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping("/orders/cooking")
    public ResponseEntity<OrderListResponse> getCookingOrders() {
        OrderListResponse response = orderService.getCookingOrders();
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/orders/delivery")
    public ResponseEntity<DeliveryOrderListResponse> getDeliveryOrders() {
        DeliveryOrderListResponse response = orderService.getDeliveryOrders();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/orders/customer/{customerId}")
    public ResponseEntity<Map<String, Object>> getOrdersByCustomerId(@PathVariable Long customerId) {
        SecurityUtils.validateCustomerAccess(customerId);
        Map<String, Object> response = orderService.getOrdersByCustomerIdForResponse(customerId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/orders/{id}/status")
    public ResponseEntity<OrderUpdateResponse> updateOrderStatus(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        OrderUpdateResponse response = orderService.updateOrderStatus(id, request);
        return ResponseEntity.ok(response);
    }
}


