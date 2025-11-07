package com.dinnervery.controller;

import com.dinnervery.dto.request.OrderItemCreateRequest;
import com.dinnervery.dto.order.OrderResponse;
import com.dinnervery.dto.request.OrderCreateRequest;
import com.dinnervery.dto.request.OrderPreviewRequest;
import com.dinnervery.dto.request.PriceCalculationRequest;
import com.dinnervery.dto.response.OrderPreviewResponse;
import com.dinnervery.dto.response.PriceCalculationResponse;
import com.dinnervery.dto.response.OrderListResponse;
import com.dinnervery.dto.response.DeliveryOrderListResponse;
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

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    // 가격계산 API
    @PostMapping("/orders/calculate-price")
    public ResponseEntity<PriceCalculationResponse> calculatePrice(@RequestBody PriceCalculationRequest request) {
        // 고객 조회
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new IllegalArgumentException("고객을 찾을 수 없습니다: " + request.getCustomerId()));
        
        // 총액 계산
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
        
        // 할인 전 총액만 반환 (할인은 프론트엔드에서 처리)
        PriceCalculationResponse response = new PriceCalculationResponse(
                subtotal,
                null,
                subtotal,
                customer.getGrade().toString(),
                0
        );
        
        return ResponseEntity.ok(response);
    }

    // 주문 미리보기 API (가격계산 + 주문 내역 확인)
    @PostMapping("/orders/preview")
    public ResponseEntity<OrderPreviewResponse> previewOrder(@Valid @RequestBody OrderPreviewRequest request) {
        List<OrderPreviewResponse.ItemResponse> itemResponses = new ArrayList<>();
        int itemsTotalPrice = 0;
        
        // 각 아이템별 처리
        for (OrderPreviewRequest.ItemRequest itemRequest : request.getItems()) {
            // 메뉴 조회
            Menu menu = menuRepository.findById(itemRequest.getMenuId())
                    .orElseThrow(() -> new IllegalArgumentException("메뉴를 찾을 수 없습니다: " + itemRequest.getMenuId()));
            
            // 서빙 스타일 조회 (필수)
            ServingStyle servingStyle = servingStyleRepository.findById(itemRequest.getServingStyleId())
                    .orElseThrow(() -> new IllegalArgumentException("서빙 스타일을 찾을 수 없습니다: " + itemRequest.getServingStyleId()));
            
            // 아이템 기본 가격 계산 (메뉴 가격 + 서빙 스타일 추가 가격)
            int basePrice = menu.getPrice() + servingStyle.getExtraPrice();
            
            // 옵션 처리
            List<OrderPreviewResponse.OptionResponse> optionResponses = new ArrayList<>();
            int optionPrice = 0;
            
            if (itemRequest.getOptions() != null && !itemRequest.getOptions().isEmpty()) {
                for (OrderPreviewRequest.OptionRequest optionRequest : itemRequest.getOptions()) {
                    MenuOption menuOption = menuOptionRepository.findById(optionRequest.getOptionId())
                            .orElseThrow(() -> new IllegalArgumentException("옵션을 찾을 수 없습니다: " + optionRequest.getOptionId()));
                    
                    // 옵션 정보 추가
                    optionResponses.add(new OrderPreviewResponse.OptionResponse(
                            menuOption.getItemName(),
                            optionRequest.getQuantity()
                    ));
                    
                    // 옵션 가격 계산 (옵션 수량 * 옵션 가격)
                    optionPrice += menuOption.getItemPrice() * optionRequest.getQuantity();
                }
            }
            
            // 아이템 총 가격 계산 (기본 가격 + 옵션 가격) * 수량
            int itemPrice = (basePrice + optionPrice) * itemRequest.getQuantity();
            itemsTotalPrice += itemPrice;
            
            // 아이템 응답 생성
            itemResponses.add(new OrderPreviewResponse.ItemResponse(
                    menu.getName(),
                    itemRequest.getQuantity(),
                    itemPrice,
                    servingStyle.getName(),
                    optionResponses
            ));
        }
        
        // 응답 생성
        OrderPreviewResponse response = new OrderPreviewResponse(
                itemResponses,
                itemsTotalPrice
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
                                    orderItem.getOrderItemOptions().stream().map(opt ->
                                            new OrderListResponse.OrderSummary.OptionSummaryDto(
                                                    opt.getMenuOption().getId(),
                                                    opt.getMenuOption().getItemName(),
                                                    opt.getQuantity()
                                            )
                                    ).collect(java.util.stream.Collectors.toList())
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
    public ResponseEntity<DeliveryOrderListResponse> getDeliveryOrders() {
        List<Order> orders = orderRepository.findByDeliveryStatusIn(List.of(Order.Status.COOKED, Order.Status.DELIVERING));

        List<DeliveryOrderListResponse.OrderSummary> orderList = orders.stream()
                .map(order -> {
                    List<DeliveryOrderListResponse.OrderSummary.OrderItem> orderItems = order.getOrderItems().stream()
                            .map(orderItem -> new DeliveryOrderListResponse.OrderSummary.OrderItem(
                                    orderItem.getMenu().getId(),
                                    orderItem.getMenu().getName(),
                                    orderItem.getQuantity(),
                                    orderItem.getServingStyle().getId(),
                                    orderItem.getServingStyle().getName(),
                                    orderItem.getOrderItemOptions().stream().map(opt ->
                                            new DeliveryOrderListResponse.OrderSummary.OptionSummaryDto(
                                                    opt.getMenuOption().getId(),
                                                    opt.getMenuOption().getItemName(),
                                                    opt.getQuantity()
                                            )
                                    ).collect(java.util.stream.Collectors.toList())
                            ))
                            .collect(Collectors.toList());
                    
                    return new DeliveryOrderListResponse.OrderSummary(
                            order.getId(),
                            order.getDeliveryStatus().toString(),
                            order.getDeliveryTime().toString(),
                            order.getAddress().getAddress(),
                            orderItems
                    );
                })
                .collect(Collectors.toList());
        
        DeliveryOrderListResponse response = new DeliveryOrderListResponse(orderList);
        
        return ResponseEntity.ok(response);
    }

    // 관리자용 주문 관리(상세 API 응답)
    @PostMapping("/orders")
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody OrderCreateRequest request) {
        OrderResponse response = orderService.createOrder(request);
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
        
        // 모든 상태의 주문을 표시
        List<Map<String, Object>> orderList = orders.stream()
                .map(order -> {
                    Map<String, Object> orderMap = new HashMap<>();
                    orderMap.put("orderId", order.getId());
                    
                    // 날짜 형식: "2025.09.11"
                    String orderDate = order.getCreatedAt().toLocalDate().toString()
                            .replace("-", ".");
                    orderMap.put("orderDate", orderDate);
                    
                    orderMap.put("totalPrice", order.getTotalPrice());
                    orderMap.put("status", order.getDeliveryStatus().toString());
                    orderMap.put("deliveryTime", order.getDeliveryTime().toString());
                    
                    // 주문 아이템 목록
                    List<Map<String, Object>> orderItemsList = order.getOrderItems().stream()
                            .map(orderItem -> {
                                Map<String, Object> itemMap = new HashMap<>();
                                itemMap.put("name", orderItem.getMenu().getName()); // 메뉴명
                                itemMap.put("quantity", orderItem.getQuantity());
                                
                                // 서빙 스타일명 (예시에서 두 번째 name으로 표시)
                                if (orderItem.getServingStyle() != null) {
                                    itemMap.put("styleName", orderItem.getServingStyle().getName());
                                }
                                
                                // 옵션 목록
                                List<Map<String, Object>> optionsList = orderItem.getOrderItemOptions().stream()
                                        .map(option -> {
                                            Map<String, Object> optionMap = new HashMap<>();
                                            optionMap.put("name", option.getMenuOption().getItemName());
                                            optionMap.put("quantity", option.getQuantity());
                                            return optionMap;
                                        })
                                        .collect(Collectors.toList());
                                itemMap.put("options", optionsList);
                                
                                return itemMap;
                            })
                            .collect(Collectors.toList());
                    orderMap.put("orderItems", orderItemsList);
                    
                    return orderMap;
                })
                .collect(Collectors.toList());
        
        Map<String, Object> response = new HashMap<>();
        response.put("orders", orderList);
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/orders/{id}/complete")
    public ResponseEntity<OrderResponse> completeOrder(@PathVariable Long id) {
        OrderResponse response = orderService.completeOrder(id);
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
                savedOrder.getDeliveryStatus().toString()
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
            ServingStyle servingStyle = servingStyleRepository.findById(itemRequest.getStyleId()).get();

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
            if (!servingStyleRepository.existsById(itemRequest.getStyleId())) {
                throw new IllegalArgumentException("서빙 스타일을 찾을 수 없습니다: " + itemRequest.getStyleId());
            }
        }
    }
}


