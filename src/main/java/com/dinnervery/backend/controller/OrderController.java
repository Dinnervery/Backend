package com.dinnervery.backend.controller;

import com.dinnervery.backend.dto.OrderDto;
import com.dinnervery.backend.dto.order.OrderItemOptionRequest;
import com.dinnervery.backend.dto.order.OrderItemRequest;
import com.dinnervery.backend.dto.order.OrderResponse;
import com.dinnervery.backend.dto.request.OrderCreateRequest;
import com.dinnervery.backend.dto.request.ReorderRequest;
import com.dinnervery.backend.entity.Customer;
import com.dinnervery.backend.entity.Employee;
import com.dinnervery.backend.repository.CustomerRepository;
import com.dinnervery.backend.repository.MenuOptionRepository;
import com.dinnervery.backend.entity.Menu;
import com.dinnervery.backend.entity.MenuOption;
import com.dinnervery.backend.repository.MenuRepository;
import com.dinnervery.backend.repository.OrderItemOptionRepository;
import com.dinnervery.backend.entity.Order;
import com.dinnervery.backend.entity.OrderItem;
import com.dinnervery.backend.entity.OrderItemOption;
import com.dinnervery.backend.repository.OrderItemRepository;
import com.dinnervery.backend.repository.OrderRepository;
import com.dinnervery.backend.repository.ServingStyleRepository;
import com.dinnervery.backend.service.OrderService;
import com.dinnervery.backend.service.PriceCalculator;
import com.dinnervery.backend.common.annotation.RequireDuty;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderItemOptionRepository orderItemOptionRepository;
    private final CustomerRepository customerRepository;
    private final MenuRepository menuRepository;
    private final ServingStyleRepository servingStyleRepository;
    private final MenuOptionRepository menuOptionRepository;
    private final PriceCalculator priceCalculator;

    // 서비스를 통한 주문 관리 (외부 API 응답용)
    @PostMapping("/orders")
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody OrderCreateRequest request) {
        OrderDto orderDto = orderService.createOrder(request);
        Order order = orderRepository.findByIdWithDetails(orderDto.getId())
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다: " + orderDto.getId()));
        OrderResponse response = OrderResponse.from(order);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/orders/{id}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable Long id) {
        Order order = orderRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다: " + id));
        OrderResponse response = OrderResponse.from(order);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/orders/customer/{customerId}")
    public ResponseEntity<List<OrderResponse>> getOrdersByCustomerId(@PathVariable Long customerId) {
        List<Order> orders = orderRepository.findByCustomerIdWithDetails(customerId);
        List<OrderResponse> responses = orders.stream()
                .map(OrderResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/orders")
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        List<Order> orders = orderRepository.findAll();
        List<OrderResponse> responses = orders.stream()
                .map(OrderResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @PostMapping("/orders/{id}/complete")
    public ResponseEntity<OrderResponse> completeOrder(@PathVariable Long id) {
        OrderDto orderDto = orderService.completeOrder(id);
        Order order = orderRepository.findByIdWithDetails(orderDto.getId())
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다: " + orderDto.getId()));
        OrderResponse response = OrderResponse.from(order);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/orders/{id}")
    public ResponseEntity<Void> cancelOrder(@PathVariable Long id) {
        orderService.cancelOrder(id);
        return ResponseEntity.noContent().build();
    }

    // 권한 체크가 포함된 주문 상태 변경 API들
    @PatchMapping("/orders/{id}/start-cooking")
    @RequireDuty({Employee.EmployeeTask.COOK})
    public ResponseEntity<Map<String, Object>> startCooking(@PathVariable Long id) {
        try {
            Order order = orderRepository.findByIdWithDetails(id)
                    .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다: " + id));
            
            order.startCooking();
            Order savedOrder = orderRepository.save(order);
            
            Map<String, Object> response = new HashMap<>();
            response.put("orderId", savedOrder.getId());
            response.put("status", savedOrder.getDeliveryStatus());
            response.put("cookingStartedAt", savedOrder.getCookingAt());
            response.put("message", "조리가 시작되었습니다.");
            
            return ResponseEntity.ok(response);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    @PatchMapping("/orders/{id}/handover")
    @RequireDuty({Employee.EmployeeTask.COOK})
    public ResponseEntity<Map<String, Object>> handoverToDelivery(@PathVariable Long id) {
        try {
            Order order = orderRepository.findByIdWithDetails(id)
                    .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다: " + id));
            
            order.handoverToDelivery();
            Order savedOrder = orderRepository.save(order);
            
            Map<String, Object> response = new HashMap<>();
            response.put("orderId", savedOrder.getId());
            response.put("status", savedOrder.getDeliveryStatus());
            response.put("handoverAt", savedOrder.getHandoverAt());
            response.put("message", "배달팀에 인수되었습니다.");
            
            return ResponseEntity.ok(response);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    @PatchMapping("/orders/{id}/start-delivery")
    @RequireDuty({Employee.EmployeeTask.DELIVERY})
    public ResponseEntity<Map<String, Object>> startDelivery(@PathVariable Long id) {
        try {
            Order order = orderRepository.findByIdWithDetails(id)
                    .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다: " + id));
            
            order.startDelivering();
            Order savedOrder = orderRepository.save(order);
            
            Map<String, Object> response = new HashMap<>();
            response.put("orderId", savedOrder.getId());
            response.put("status", savedOrder.getDeliveryStatus());
            response.put("deliveryStartedAt", savedOrder.getDeliveringAt());
            response.put("message", "배달이 시작되었습니다.");
            
            return ResponseEntity.ok(response);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    @PatchMapping("/orders/{id}/done")
    @RequireDuty({Employee.EmployeeTask.DELIVERY})
    public ResponseEntity<Map<String, Object>> completeDelivery(@PathVariable Long id) {
        try {
            Order order = orderRepository.findByIdWithDetails(id)
                    .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다: " + id));
            
            order.completeDelivery();
            Order savedOrder = orderRepository.save(order);
            
            // 주문 완료 시 고객 주문수 증가 및 등급 갱신
            Customer customer = savedOrder.getCustomer();
            customer.incrementOrderCount();
            customerRepository.save(customer);
            
            Map<String, Object> response = new HashMap<>();
            response.put("orderId", savedOrder.getId());
            response.put("status", savedOrder.getDeliveryStatus());
            response.put("completedAt", savedOrder.getDoneAt());
            response.put("customerOrderCount", customer.getOrderCount());
            response.put("customerGrade", customer.getGrade());
            response.put("message", "배달이 완료되었습니다.");
            
            return ResponseEntity.ok(response);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    // 간편 재주문 API
    @PostMapping("/orders/{id}/reorder")
    public ResponseEntity<OrderResponse> reorder(@PathVariable Long id, @Valid @RequestBody ReorderRequest request) {
        OrderDto orderDto = orderService.reorder(id, request);
        Order order = orderRepository.findByIdWithDetails(orderDto.getId())
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다: " + orderDto.getId()));
        OrderResponse response = OrderResponse.from(order);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 직접적인 주문 생성 (상세 로직 포함)
    @PostMapping("/orders/direct")
    public ResponseEntity<OrderResponse> createOrderDirect(@RequestBody com.dinnervery.backend.dto.order.OrderCreateRequest request) {
        // 1) 메뉴/서빙스타일/옵션 존재 검증
        validateOrderItems(request);

        // 2) 총 가격 계산
        int totalPrice = priceCalculator.calcOrderTotal(request);

        // 3) 고객 조회
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new IllegalArgumentException("고객을 찾을 수 없습니다: " + request.getCustomerId()));

        // 4) 주문 생성
        Order order = Order.builder()
                .customer(customer)
                .build();

        Order savedOrder = orderRepository.save(order);

        // 5) 주문 항목들 생성
        for (OrderItemRequest itemRequest : request.getItems()) {
            Menu menu = menuRepository.findById(itemRequest.getMenuId()).get();

            OrderItem orderItem = OrderItem.builder()
                    .menu(menu)
                    .orderedQty(itemRequest.getOrderedQty())
                    .build();

            order.addOrderItem(orderItem);
            orderItemRepository.save(orderItem);

            // 주문 항목 옵션들 생성
            if (itemRequest.getOptions() != null) {
                for (OrderItemOptionRequest optionRequest : itemRequest.getOptions()) {
                    MenuOption menuOption = menuOptionRepository.findById(optionRequest.getMenuOptionId()).get();

                    OrderItemOption orderItemOption = OrderItemOption.builder()
                            .menuOption(menuOption)
                            .orderedQty(optionRequest.getOrderedQty())
                            .build();

                    orderItem.addOrderItemOption(orderItemOption);
                    orderItemOptionRepository.save(orderItemOption);
                }
            }
        }

        // 6) 고객 주문 횟수 증가 및 등급 업데이트
        customer.incrementOrderCount();
        customerRepository.save(customer);

        // 7) 응답 생성
        OrderResponse response = OrderResponse.builder()
                .orderId(savedOrder.getId())
                .totalAmount(totalPrice)
                .orderDate(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 주문 상세 정보 조회
    @GetMapping("/orders/detail/{id}")
    public ResponseEntity<Map<String, Object>> getOrderDetailById(@PathVariable Long id) {
        Order order = orderRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다: " + id));

        Map<String, Object> response = new HashMap<>();
        response.put("orderId", order.getId());
        response.put("customerId", order.getCustomer().getId());
        response.put("customerName", order.getCustomer().getName());
        response.put("orderDate", order.getCreatedAt());
        response.put("totalAmount", order.getTotalAmount());
        response.put("finalAmount", order.getFinalAmount());
        response.put("discountAmount", order.getDiscountAmount());
        response.put("modifiedAt", order.getUpdatedAt());

        return ResponseEntity.ok(response);
    }

    // 고객별 주문 목록 조회
    @GetMapping("/customers/{id}/orders")
    public ResponseEntity<List<Map<String, Object>>> getOrdersByCustomer(@PathVariable Long id) {
        List<Order> orders = orderRepository.findByCustomerIdWithDetails(id);

        List<Map<String, Object>> response = orders.stream()
                .map(order -> {
                    Map<String, Object> orderMap = new HashMap<>();
                    orderMap.put("orderId", order.getId());
                    orderMap.put("orderDate", order.getCreatedAt());
                    orderMap.put("totalAmount", order.getTotalAmount());
                    orderMap.put("finalAmount", order.getFinalAmount());
                    return orderMap;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    private void validateOrderItems(com.dinnervery.backend.dto.order.OrderCreateRequest request) {
        for (OrderItemRequest itemRequest : request.getItems()) {
            // 메뉴 존재 검증
            if (!menuRepository.existsById(itemRequest.getMenuId())) {
                throw new IllegalArgumentException("메뉴를 찾을 수 없습니다: " + itemRequest.getMenuId());
            }

            // 서빙 스타일 존재 검증
            if (!servingStyleRepository.existsById(itemRequest.getServingStyleId())) {
                throw new IllegalArgumentException("서빙 스타일을 찾을 수 없습니다: " + itemRequest.getServingStyleId());
            }

            // 옵션 존재 검증
            if (itemRequest.getOptions() != null) {
                for (OrderItemOptionRequest optionRequest : itemRequest.getOptions()) {
                    if (!menuOptionRepository.existsById(optionRequest.getMenuOptionId())) {
                        throw new IllegalArgumentException("메뉴 옵션을 찾을 수 없습니다: " + optionRequest.getMenuOptionId());
                    }
                }
            }
        }
    }
}


