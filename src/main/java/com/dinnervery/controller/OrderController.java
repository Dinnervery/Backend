package com.dinnervery.controller;

import com.dinnervery.dto.OrderDto;
import com.dinnervery.dto.request.OrderItemCreateRequest;
import com.dinnervery.dto.order.OrderResponse;
import com.dinnervery.dto.request.OrderCreateRequest;
import com.dinnervery.dto.request.PriceCalculationRequest;
import com.dinnervery.dto.request.ReorderRequest;
import com.dinnervery.dto.response.PriceCalculationResponse;
import com.dinnervery.dto.response.OrderListResponse;
import com.dinnervery.dto.response.OrderStatusResponse;
import com.dinnervery.dto.response.OrderUpdateResponse;
import com.dinnervery.entity.Address;
import com.dinnervery.entity.Customer;
import com.dinnervery.repository.AddressRepository;
import com.dinnervery.repository.CustomerRepository;
import com.dinnervery.repository.MenuOptionRepository;
import com.dinnervery.entity.Menu;
import com.dinnervery.entity.MenuOption;
import com.dinnervery.entity.ServingStyle;
import com.dinnervery.repository.MenuRepository;
import com.dinnervery.repository.ServingStyleRepository;
import com.dinnervery.entity.Order;
import com.dinnervery.entity.OrderItem;
import com.dinnervery.entity.OrderItemOption;
import com.dinnervery.repository.OrderItemRepository;
import com.dinnervery.repository.OrderRepository;
import com.dinnervery.service.OrderService;
import com.dinnervery.service.EmployeeAvailabilityService;

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
    private final AddressRepository addressRepository;
    private final MenuRepository menuRepository;
    private final ServingStyleRepository servingStyleRepository;
    private final MenuOptionRepository menuOptionRepository;
    private final EmployeeAvailabilityService employeeAvailabilityService;

    // 가격계산 API
    @PostMapping("/orders/calculate-price")
    public ResponseEntity<PriceCalculationResponse> calculatePrice(@RequestBody PriceCalculationRequest request) {
        // 고객 조회
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new IllegalArgumentException("고객을 찾을 수 없습니다: " + request.getCustomerId()));
        
        // 고객별 할인 계산
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
        
        // VIP 고객 할인
        int discountAmount = 0;
        int finalPrice = subtotal;
        int discountRate = 0;
        
        if (customer.getGrade() == Customer.CustomerGrade.VIP && customer.isVipDiscountEligible()) {
            discountRate = 10;
            discountAmount = (int) (subtotal * 0.1);
            finalPrice = subtotal - discountAmount;
        }
        
        PriceCalculationResponse response = new PriceCalculationResponse(
                subtotal,
                discountAmount > 0 ? discountAmount : null,
                finalPrice,
                customer.getGrade().toString(),
                discountRate
        );
        
        return ResponseEntity.ok(response);
    }

    // 조리 대기목록 조회 (요리직원용)
    @GetMapping("/orders/cooking")
    public ResponseEntity<OrderListResponse> getCookingOrders() {
        List<Order> orders = orderRepository.findByDeliveryStatusIn(List.of(Order.Status.REQUESTED, Order.Status.COOKING));
        
        List<OrderListResponse.OrderSummary> orderList = orders.stream()
                .map(order -> {
                    List<OrderListResponse.OrderSummary.OrderedItem> orderedItems = order.getOrderItems().stream()
                            .map(orderItem -> new OrderListResponse.OrderSummary.OrderedItem(
                                    orderItem.getMenu().getId(),
                                    orderItem.getMenu().getName(),
                                    orderItem.getQuantity(),
                                    orderItem.getServingStyle().getId(),
                                    orderItem.getServingStyle().getName(),
                                    List.of() // 현재는 빈 리스트
                            ))
                            .collect(Collectors.toList());
                    
                    return new OrderListResponse.OrderSummary(
                            order.getId(),
                            order.getDeliveryStatus().toString(),
                            order.getDeliveryTime().toString(),
                            orderedItems
                    );
                })
                .collect(Collectors.toList());
        
        OrderListResponse response = new OrderListResponse(orderList);
        
        return ResponseEntity.ok(response);
    }
    
    // 배달 대기목록 조회 (배달직원용)
    @GetMapping("/orders/delivery")
    public ResponseEntity<OrderListResponse> getDeliveryOrders() {
        List<Order> orders = orderRepository.findByDeliveryStatusIn(List.of(Order.Status.COOKED, Order.Status.DELIVERING));
        
        List<OrderListResponse.OrderSummary> orderList = orders.stream()
                .map(order -> {
                    List<OrderListResponse.OrderSummary.OrderedItem> orderedItems = order.getOrderItems().stream()
                            .map(orderItem -> new OrderListResponse.OrderSummary.OrderedItem(
                                    orderItem.getMenu().getId(),
                                    orderItem.getMenu().getName(),
                                    orderItem.getQuantity(),
                                    orderItem.getServingStyle().getId(),
                                    orderItem.getServingStyle().getName(),
                                    List.of() // 현재는 빈 리스트
                            ))
                            .collect(Collectors.toList());
                    
                    return new OrderListResponse.OrderSummary(
                            order.getId(),
                            order.getDeliveryStatus().toString(),
                            order.getDeliveryTime().toString(),
                            orderedItems
                    );
                })
                .collect(Collectors.toList());
        
        OrderListResponse response = new OrderListResponse(orderList);
        
        return ResponseEntity.ok(response);
    }

    // 관리자용 주문 관리(상세 API 응답)
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
                .filter(order -> order.getDeliveryStatus() != Order.Status.DONE)
                .collect(Collectors.toList());
        
        List<Map<String, Object>> orderList = activeOrders.stream()
                .map(order -> {
                    Map<String, Object> orderMap = new HashMap<>();
                    orderMap.put("orderId", order.getId());
                    orderMap.put("orderDate", order.getCreatedAt().toLocalDate().toString().replace("-", "."));
                    orderMap.put("status", order.getDeliveryStatus());
                    orderMap.put("totalPrice", order.getFinalPrice());
                    
                    // 주문 요약 생성
                    StringBuilder summary = new StringBuilder();
                    for (OrderItem orderItem : order.getOrderItems()) {
                        summary.append(orderItem.getMenu().getName())
                               .append(" ").append(orderItem.getQuantity()).append("개");
                        
                        if (!orderItem.getOrderItemOptions().isEmpty()) {
                            summary.append("(");
                            for (int i = 0; i < orderItem.getOrderItemOptions().size(); i++) {
                                OrderItemOption option = orderItem.getOrderItemOptions().get(i);
                                summary.append(option.getMenuOption().getItemName())
                                       .append(" ").append(option.getQuantity());
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


    // 통합된 주문 상태 변경 API
    @PatchMapping("/orders/{id}/status")
    public ResponseEntity<OrderUpdateResponse> updateOrderStatus(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        Order order = orderRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다: " + id));
        
        String newStatus = (String) request.get("status");
        
        // 상태별 다른 처리
        switch (newStatus) {
            case "COOKING":
                // 가능한 요리사가 있는지 확인
                if (!employeeAvailabilityService.hasAvailableCook()) {
                    throw new IllegalStateException("현재 가능한 요리사가 없습니다. 잠시 후 다시 시도해주세요.");
                }
                order.startCooking();
                break;
            case "COOKED":
                order.completeCooking();
                break;
            case "DELIVERING":
                // 가능한 배달원이 있는지 확인
                if (!employeeAvailabilityService.hasAvailableDeliveryPerson()) {
                    throw new IllegalStateException("현재 가능한 배달원이 없습니다. 잠시 후 다시 시도해주세요.");
                }
                order.startDelivering();
                break;
            case "DONE":
                order.completeDelivery();
                // 주문 완료 시 고객 주문 수 증가 및 등급 갱신
                Customer customer = order.getCustomer();
                customer.incrementOrderCount();
                customerRepository.save(customer);
                break;
            default:
                throw new IllegalArgumentException("유효하지 않은 주문 상태입니다: " + newStatus);
        }
        
        Order savedOrder = orderRepository.save(order);
        
        OrderUpdateResponse response = new OrderUpdateResponse(
                savedOrder.getId(),
                savedOrder.getDeliveryStatus().toString(),
                LocalDateTime.now()
        );
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/orders/{id}/status")
    public ResponseEntity<OrderStatusResponse> getOrderStatus(@PathVariable Long id) {
        Order order = orderRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다: " + id));
        
        // 주문 아이템들을 메뉴-서빙스타일 묶음으로 구성
        List<OrderStatusResponse.OrderItemDetail> orderItems = new ArrayList<>();
        
        for (OrderItem orderItem : order.getOrderItems()) {
            // 옵션들
            List<OrderStatusResponse.OrderItemDetail.OptionDetail> options = new ArrayList<>();
            for (OrderItemOption option : orderItem.getOrderItemOptions()) {
                OrderStatusResponse.OrderItemDetail.OptionDetail optionDetail = 
                        new OrderStatusResponse.OrderItemDetail.OptionDetail(
                                option.getMenuOption().getId(),
                                option.getMenuOption().getItemName(),
                                option.getQuantity(),
                                option.getMenuOption().getItemPrice()
                        );
                options.add(optionDetail);
            }
            
            OrderStatusResponse.OrderItemDetail itemDetail = 
                    new OrderStatusResponse.OrderItemDetail(
                            orderItem.getMenu().getId(),
                            orderItem.getMenu().getName(),
                            orderItem.getQuantity(),
                            orderItem.getMenu().getPrice(),
                            orderItem.getServingStyle() != null ? orderItem.getServingStyle().getId() : null,
                            orderItem.getServingStyle() != null ? orderItem.getServingStyle().getName() : null,
                            orderItem.getServingStyle() != null ? orderItem.getServingStyle().getExtraPrice() : 0,
                            options
                    );
            orderItems.add(itemDetail);
        }
        
        OrderStatusResponse response = new OrderStatusResponse(
                order.getId(),
                order.getDeliveryStatus().toString(),
                order.getDeliveryTime().toString(),
                orderItems,
                order.getCreatedAt(),
                order.getDoneAt()
        );
        
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
    public ResponseEntity<OrderResponse> createOrderDirect(@RequestBody OrderCreateRequest request) {
        // 1) 메뉴/서빙스타일/옵션 존재 검증
        validateOrderItems(request);

        // 2) 고객 조회
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new IllegalArgumentException("고객을 찾을 수 없습니다: " + request.getCustomerId()));

        // 주소 조회
        Address address = addressRepository.findById(request.getAddressId())
                .orElseThrow(() -> new IllegalArgumentException("주소를 찾을 수 없습니다: " + request.getAddressId()));

        // 3) 주문 생성
        Order order = Order.builder()
                .customer(customer)
                .address(address)
                .cardNumber(request.getCardNumber())
                .deliveryTime(request.getDeliveryTime())
                .build();

        Order savedOrder = orderRepository.save(order);

        // 4) 주문 아이템들 생성
        for (OrderItemCreateRequest itemRequest : request.getOrderItems()) {
            Menu menu = menuRepository.findById(itemRequest.getMenuId()).get();
            ServingStyle servingStyle = servingStyleRepository.findById(itemRequest.getServingStyleId()).get();

            OrderItem orderItem = OrderItem.builder()
                    .menu(menu)
                    .servingStyle(servingStyle)
                    .quantity(itemRequest.getQuantity())
                    .build();

            order.addOrderItem(orderItem);
            orderItemRepository.save(orderItem);
        }

        // 5) 고객 주문 수 증가 및 등급 업데이트
        customer.incrementOrderCount();
        customerRepository.save(customer);

        // 6) 응답 생성
        OrderResponse response = OrderResponse.from(savedOrder);

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
        response.put("totalPrice", order.getTotalPrice());
        response.put("finalPrice", order.getFinalPrice());
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
                    orderMap.put("totalPrice", order.getTotalPrice());
                    orderMap.put("finalPrice", order.getFinalPrice());
                    return orderMap;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    private void validateOrderItems(OrderCreateRequest request) {
        for (OrderItemCreateRequest itemRequest : request.getOrderItems()) {
            // 메뉴 존재 검증
            if (!menuRepository.existsById(itemRequest.getMenuId())) {
                throw new IllegalArgumentException("메뉴를 찾을 수 없습니다: " + itemRequest.getMenuId());
            }

            // 서빙 스타일 존재 검증
            if (!servingStyleRepository.existsById(itemRequest.getServingStyleId())) {
                throw new IllegalArgumentException("서빙 스타일을 찾을 수 없습니다: " + itemRequest.getServingStyleId());
            }
        }
    }
}


