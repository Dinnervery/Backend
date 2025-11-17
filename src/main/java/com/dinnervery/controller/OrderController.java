package com.dinnervery.controller;

import com.dinnervery.dto.order.response.OrderListResponse;
import com.dinnervery.dto.order.response.DeliveryOrderListResponse;
import com.dinnervery.dto.order.response.OrderUpdateResponse;
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

    // 조리 대기목록 조회 (요리직원용)
    @GetMapping("/orders/cooking")
    public ResponseEntity<OrderListResponse> getCookingOrders() {
        OrderListResponse response = orderService.getCookingOrders();
        return ResponseEntity.ok(response);
    }
    
    // 배달 대기목록 조회 (배달직원용)
    @GetMapping("/orders/delivery")
    public ResponseEntity<DeliveryOrderListResponse> getDeliveryOrders() {
        DeliveryOrderListResponse response = orderService.getDeliveryOrders();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/orders/customer/{customerId}")
    public ResponseEntity<Map<String, Object>> getOrdersByCustomerId(@PathVariable Long customerId) {
        Map<String, Object> response = orderService.getOrdersByCustomerIdForResponse(customerId);
        return ResponseEntity.ok(response);
    }

    // 통합된 주문 상태 변경 API
    @PatchMapping("/orders/{id}/status")
    public ResponseEntity<OrderUpdateResponse> updateOrderStatus(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        OrderUpdateResponse response = orderService.updateOrderStatus(id, request);
        return ResponseEntity.ok(response);
    }

}


