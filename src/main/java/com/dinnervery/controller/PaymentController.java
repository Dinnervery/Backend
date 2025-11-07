package com.dinnervery.controller;

import com.dinnervery.dto.order.response.OrderResponse;
import com.dinnervery.dto.order.request.OrderCreateRequest;
import com.dinnervery.service.OrderService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PaymentController {

    private final OrderService orderService;

    // 관리자용 주문 관리(상세 API 응답)
    @PostMapping("/orders")
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody OrderCreateRequest request) {
        OrderResponse response = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}

