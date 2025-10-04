package com.dinnervery.backend.controller;

import com.dinnervery.backend.dto.OrderDto;
import com.dinnervery.backend.dto.order.OrderItemOptionRequest;
import com.dinnervery.backend.dto.order.OrderItemRequest;
import com.dinnervery.backend.dto.order.OrderResponse;
import com.dinnervery.backend.dto.request.OrderCreateRequest;
import com.dinnervery.backend.dto.request.PriceCalculationRequest;
import com.dinnervery.backend.dto.request.ReorderRequest;
import com.dinnervery.backend.entity.Customer;
import com.dinnervery.backend.entity.Employee;
import com.dinnervery.backend.repository.CustomerRepository;
import com.dinnervery.backend.repository.MenuOptionRepository;
import com.dinnervery.backend.entity.Menu;
import com.dinnervery.backend.entity.MenuOption;
import com.dinnervery.backend.entity.ServingStyle;
import com.dinnervery.backend.repository.MenuRepository;
import com.dinnervery.backend.repository.ServingStyleRepository;
import com.dinnervery.backend.entity.Order;
import com.dinnervery.backend.entity.OrderItem;
import com.dinnervery.backend.entity.OrderItemOption;
import com.dinnervery.backend.entity.Task;
import com.dinnervery.backend.repository.OrderItemRepository;
import com.dinnervery.backend.repository.OrderRepository;
import com.dinnervery.backend.repository.EmployeeRepository;
import com.dinnervery.backend.repository.TaskRepository;
import com.dinnervery.backend.service.OrderService;
import com.dinnervery.backend.service.PriceCalculator;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
    private final CustomerRepository customerRepository;
    private final MenuRepository menuRepository;
    private final ServingStyleRepository servingStyleRepository;
    private final MenuOptionRepository menuOptionRepository;
    private final PriceCalculator priceCalculator;

    // 가격 계산 API
    @PostMapping("/orders/calculate-price")
    public ResponseEntity<Map<String, Object>> calculatePrice(@RequestBody PriceCalculationRequest request) {
        // 고객 조회
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new IllegalArgumentException("고객을 찾을 수 없습니다: " + request.getCustomerId()));
        
        // 할인 전 총 가격 계산
        int subtotal = 0;
        for (PriceCalculationRequest.OrderItemRequest item : request.getItems()) {
            Menu menu = menuRepository.findById(item.getMenuId())
                    .orElseThrow(() -> new IllegalArgumentException("메뉴를 찾을 수 없습니다: " + item.getMenuId()));
            
            ServingStyle servingStyle = servingStyleRepository.findById(item.getServingStyleId())
                    .orElseThrow(() -> new IllegalArgumentException("서빙 스타일을 찾을 수 없습니다: " + item.getServingStyleId()));
            
            subtotal += (menu.getPrice() + servingStyle.getExtraPrice()) * item.getQuantity();
            
            // 옵션 가격 추가
            if (item.getOptionIds() != null) {
                for (Long optionId : item.getOptionIds()) {
                    MenuOption option = menuOptionRepository.findById(optionId)
                            .orElseThrow(() -> new IllegalArgumentException("옵션을 찾을 수 없습니다: " + optionId));
                    subtotal += option.getItemPrice() * item.getQuantity();
                }
            }
        }
        
        // VIP 할인 적용
        int discountAmount = 0;
        int finalPrice = subtotal;
        int discountRate = 0;
        
        if (customer.getGrade() == Customer.CustomerGrade.VIP && customer.isVipDiscountEligible()) {
            discountRate = 10;
            discountAmount = (int) (subtotal * 0.1);
            finalPrice = subtotal - discountAmount;
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("subtotal", subtotal);
        response.put("discountAmount", discountAmount > 0 ? discountAmount : null);
        response.put("finalPrice", finalPrice);
        response.put("customerGrade", customer.getGrade().toString());
        response.put("discountRate", discountRate);
        
        return ResponseEntity.ok(response);
    }

    // 재주문 API
    @PostMapping("/orders/{id}/reorder")
    public ResponseEntity<Map<String, Object>> reorder(@PathVariable Long id) {
        Order originalOrder = orderRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new IllegalArgumentException("원본 주문을 찾을 수 없습니다: " + id));
        
        List<Map<String, Object>> orderedItems = originalOrder.getOrderItems().stream()
                .map(orderItem -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("menuId", orderItem.getMenu().getId());
                    item.put("quantity", orderItem.getOrderedQty());
                    item.put("servingStyleId", orderItem.getServingStyle().getId());
                    
                    // 옵션 ID 목록 (현재는 빈 리스트로 설정)
                    item.put("optionIds", List.of());
                    
                    return item;
                })
                .collect(Collectors.toList());
        
        Map<String, Object> response = new HashMap<>();
        response.put("orderedItems", orderedItems);
        
        return ResponseEntity.ok(response);
    }

    // 조리 대기 목록 조회 (요리직원용)
    @GetMapping("/orders/cooking")
    public ResponseEntity<Map<String, Object>> getCookingOrders() {
        List<Order> orders = orderRepository.findByDeliveryStatusIn(List.of(Order.status.REQUESTED, Order.status.COOKING));
        
        List<Map<String, Object>> orderList = orders.stream()
                .map(order -> {
                    Map<String, Object> orderMap = new HashMap<>();
                    orderMap.put("orderId", order.getId());
                    orderMap.put("status", order.getDeliveryStatus().toString());
                    orderMap.put("deliveryTime", order.getDeliveryTime().toString());
                    
                    List<Map<String, Object>> orderedItems = order.getOrderItems().stream()
                            .map(orderItem -> {
                                Map<String, Object> item = new HashMap<>();
                                item.put("menuId", orderItem.getMenu().getId());
                                item.put("name", orderItem.getMenu().getName());
                                item.put("quantity", orderItem.getOrderedQty());
                                item.put("styleId", orderItem.getServingStyle().getId());
                                item.put("styleName", orderItem.getServingStyle().getName());
                                item.put("options", List.of()); // 현재는 빈 리스트
                                return item;
                            })
                            .collect(Collectors.toList());
                    
                    orderMap.put("orderedItems", orderedItems);
                    return orderMap;
                })
                .collect(Collectors.toList());
        
        Map<String, Object> response = new HashMap<>();
        response.put("orders", orderList);
        
        return ResponseEntity.ok(response);
    }
    
    // 배달 대기 목록 조회 (배달직원용)
    @GetMapping("/orders/delivery")
    public ResponseEntity<Map<String, Object>> getDeliveryOrders() {
        List<Order> orders = orderRepository.findByDeliveryStatusIn(List.of(Order.status.COOKED, Order.status.DELIVERING));
        
        List<Map<String, Object>> orderList = orders.stream()
                .map(order -> {
                    Map<String, Object> orderMap = new HashMap<>();
                    orderMap.put("orderId", order.getId());
                    orderMap.put("status", order.getDeliveryStatus().toString());
                    orderMap.put("deliveryTime", order.getDeliveryTime().toString());
                    
                    List<Map<String, Object>> orderedItems = order.getOrderItems().stream()
                            .map(orderItem -> {
                                Map<String, Object> item = new HashMap<>();
                                item.put("menuId", orderItem.getMenu().getId());
                                item.put("name", orderItem.getMenu().getName());
                                item.put("quantity", orderItem.getOrderedQty());
                                item.put("styleId", orderItem.getServingStyle().getId());
                                item.put("styleName", orderItem.getServingStyle().getName());
                                item.put("options", List.of()); // 현재는 빈 리스트
                                return item;
                            })
                            .collect(Collectors.toList());
                    
                    orderMap.put("orderedItems", orderedItems);
                    return orderMap;
                })
                .collect(Collectors.toList());
        
        Map<String, Object> response = new HashMap<>();
        response.put("orders", orderList);
        
        return ResponseEntity.ok(response);
    }

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
    public ResponseEntity<Map<String, Object>> getOrdersByCustomerId(@PathVariable Long customerId) {
        List<Order> orders = orderRepository.findByCustomerIdWithDetails(customerId);
        
        // DONE 상태가 아닌 주문만 필터링
        List<Order> activeOrders = orders.stream()
                .filter(order -> order.getDeliveryStatus() != Order.status.DONE)
                .collect(Collectors.toList());
        
        List<Map<String, Object>> orderList = activeOrders.stream()
                .map(order -> {
                    Map<String, Object> orderMap = new HashMap<>();
                    orderMap.put("orderId", order.getId());
                    orderMap.put("orderDate", order.getCreatedAt().toLocalDate().toString().replace("-", "."));
                    orderMap.put("status", order.getDeliveryStatus());
                    orderMap.put("totalPrice", order.getFinalAmount());
                    
                    // 주문 요약 생성
                    StringBuilder summary = new StringBuilder();
                    for (OrderItem orderItem : order.getOrderItems()) {
                        summary.append(orderItem.getMenu().getName())
                               .append(" ").append(orderItem.getOrderedQty()).append("개");
                        
                        if (!orderItem.getOrderItemOptions().isEmpty()) {
                            summary.append("(");
                            for (int i = 0; i < orderItem.getOrderItemOptions().size(); i++) {
                                OrderItemOption option = orderItem.getOrderItemOptions().get(i);
                                summary.append(option.getMenuOption().getItemName())
                                       .append(" ").append(option.getOrderedQty());
                                if (i < orderItem.getOrderItemOptions().size() - 1) {
                                    summary.append(", ");
                                }
                            }
                            summary.append(")");
                        }
                        
                        if (orderItem.getServingStyle() != null) {
                            summary.append(", ").append(orderItem.getServingStyle().getName());
                        }
                    }
                    orderMap.put("orderSummary", summary.toString());
                    orderMap.put("deliveryTime", order.getDeliveryTime().toString());
                    
                    return orderMap;
                })
                .collect(Collectors.toList());
        
        Map<String, Object> response = new HashMap<>();
        response.put("orders", orderList);
        
        return ResponseEntity.ok(response);
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
    public ResponseEntity<Map<String, Object>> cancelOrder(@PathVariable Long id) {
        Order order = orderRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다: " + id));
        
        if (order.getDeliveryStatus() != Order.status.REQUESTED) {
            throw new IllegalStateException("취소 불가능한 상태입니다. 현재 상태: " + order.getDeliveryStatus());
        }
        
        order.cancelOrder();
        orderRepository.save(order);
        
        Map<String, Object> response = new HashMap<>();
        response.put("orderId", order.getId());
        response.put("status", order.getDeliveryStatus());
        response.put("cancelledAt", order.getCanceledAt());
        response.put("refundAmount", order.getFinalAmount());
        
        return ResponseEntity.ok(response);
    }

    // 통합된 주문 상태 변경 API
    @PatchMapping("/orders/{id}/status")
    public ResponseEntity<Map<String, Object>> updateOrderStatus(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        try {
            Order order = orderRepository.findByIdWithDetails(id)
                    .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다: " + id));
            
            String newStatus = (String) request.get("status");
            
            // 상태에 따른 처리
            switch (newStatus) {
                case "COOKING":
                    order.startCooking();
                    break;
                case "COOKED":
                    order.completeCooking();
                    break;
                case "DELIVERING":
                    order.startDelivering();
                    break;
                case "DONE":
                    order.completeDelivery();
                    // 주문 완료 시 고객 주문수 증가 및 등급 갱신
                    Customer customer = order.getCustomer();
                    customer.incrementOrderCount();
                    customerRepository.save(customer);
                    break;
                default:
                    throw new IllegalArgumentException("유효하지 않은 상태입니다: " + newStatus);
            }
            
            Order savedOrder = orderRepository.save(order);
            
            Map<String, Object> response = new HashMap<>();
            response.put("orderId", savedOrder.getId());
            response.put("status", savedOrder.getDeliveryStatus());
            response.put("updatedAt", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/orders/{id}/status")
    public ResponseEntity<Map<String, Object>> getOrderStatus(@PathVariable Long id) {
        Order order = orderRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다: " + id));
        
        // 주문 항목들을 디너-스타일 묶음으로 구성
        List<Map<String, Object>> orderItems = new ArrayList<>();
        
        for (OrderItem orderItem : order.getOrderItems()) {
            Map<String, Object> item = new HashMap<>();
            
            // 메뉴 정보
            item.put("menuId", orderItem.getMenu().getId());
            item.put("menuName", orderItem.getMenu().getName());
            item.put("menuQuantity", orderItem.getOrderedQty());
            item.put("menuPrice", orderItem.getMenu().getPrice());
            
            // 서빙 스타일 정보
            if (orderItem.getServingStyle() != null) {
                item.put("styleId", orderItem.getServingStyle().getId());
                item.put("styleName", orderItem.getServingStyle().getName());
                        item.put("stylePrice", orderItem.getServingStyle().getExtraPrice());
            }
            
            // 옵션들
            List<Map<String, Object>> options = new ArrayList<>();
            for (OrderItemOption option : orderItem.getOrderItemOptions()) {
                Map<String, Object> optionItem = new HashMap<>();
                optionItem.put("optionId", option.getMenuOption().getId());
                optionItem.put("optionName", option.getMenuOption().getItemName());
                optionItem.put("optionQuantity", option.getOrderedQty());
                                optionItem.put("optionPrice", option.getMenuOption().getItemPrice());
                options.add(optionItem);
            }
            item.put("options", options);
            
            orderItems.add(item);
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("orderId", order.getId());
        response.put("status", order.getDeliveryStatus());
        response.put("deliveryTime", order.getDeliveryTime().toString());
        response.put("orderItems", orderItems);
        response.put("createdAt", order.getCreatedAt());
        response.put("deliveredAt", order.getDoneAt());
        
        return ResponseEntity.ok(response);
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


