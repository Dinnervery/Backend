package com.dinnervery.controller;

import com.dinnervery.dto.order.response.OrderResponse;
import com.dinnervery.dto.order.request.PriceCalculationRequest;
import com.dinnervery.dto.order.response.OrderPreviewResponse;
import com.dinnervery.dto.order.response.PriceCalculationResponse;
import com.dinnervery.dto.order.response.OrderListResponse;
import com.dinnervery.dto.order.response.DeliveryOrderListResponse;
import com.dinnervery.dto.order.response.OrderStatusResponse;
import com.dinnervery.dto.order.response.OrderUpdateResponse;
import com.dinnervery.entity.Customer;
import com.dinnervery.repository.CustomerRepository;
import com.dinnervery.repository.MenuOptionRepository;
import com.dinnervery.entity.Menu;
import com.dinnervery.entity.MenuOption;
import com.dinnervery.entity.Style;
import com.dinnervery.repository.MenuRepository;
import com.dinnervery.repository.StyleRepository;
import com.dinnervery.entity.Order;
import com.dinnervery.entity.OrderItem;
import com.dinnervery.entity.OrderItemOption;
import com.dinnervery.entity.Cart;
import com.dinnervery.entity.CartItem;
import com.dinnervery.entity.CartItemOption;
import com.dinnervery.repository.OrderRepository;
import com.dinnervery.repository.CartRepository;
import com.dinnervery.service.OrderService;
import com.dinnervery.service.StorageService;

import lombok.RequiredArgsConstructor;
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
    private final CustomerRepository customerRepository;
    private final MenuRepository menuRepository;
    private final StyleRepository styleRepository;
    private final MenuOptionRepository menuOptionRepository;
    private final CartRepository cartRepository;
    private final StorageService storageService;

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
            
            Style style = styleRepository.findById(item.getStyleId())
                    .orElseThrow(() -> new IllegalArgumentException("스타일을 찾을 수 없습니다: " + item.getStyleId()));
            
            subtotal += (menu.getPrice() + style.getExtraPrice()) * item.getQuantity();
            
            // 옵션 가격 추가 (기본 수량을 초과하는 만큼만 계산)
            // PriceCalculationRequest는 옵션 수량 정보가 없으므로 기본 수량만 있다고 가정
            // 기본 수량이면 추가 비용은 0원이므로 이중 계산을 방지
            if (item.getOptionIds() != null) {
                for (Long optionId : item.getOptionIds()) {
                    MenuOption option = menuOptionRepository.findById(optionId)
                            .orElseThrow(() -> new IllegalArgumentException("옵션을 찾을 수 없습니다: " + optionId));
                    // 기본 수량만 있다고 가정: calculateExtraCost(기본수량) = 0
                    // 따라서 추가 비용은 0원 (메뉴 가격에 이미 포함됨)
                    subtotal += option.calculateExtraCost(option.getDefaultQty()) * item.getQuantity();
                }
            }
        }
        
        // 할인 전 총액만 반환 (할인은 프론트엔드에서 처리)
        PriceCalculationResponse response = new PriceCalculationResponse(
                subtotal,
                customer.getGrade().toString()
        );
        
        return ResponseEntity.ok(response);
    }

    // 주문 미리보기 API (가격계산 + 주문 내역 확인) - Cart 기반
    @GetMapping("/cart/{customerId}/preview")
    public ResponseEntity<OrderPreviewResponse> previewOrder(@PathVariable Long customerId) {
        // 장바구니 조회
        Cart cart = cartRepository.findByCustomer_Id(customerId)
                .orElseThrow(() -> new IllegalArgumentException("장바구니가 비어있습니다. 주문할 상품을 장바구니에 담아주세요."));
        
        // 장바구니가 비어있는지 확인
        if (cart.getCartItems() == null || cart.getCartItems().isEmpty()) {
            throw new IllegalArgumentException("장바구니가 비어있습니다. 주문할 상품을 장바구니에 담아주세요.");
        }
        
        List<OrderPreviewResponse.ItemResponse> itemResponses = new ArrayList<>();
        int itemsTotalPrice = 0;
        
        // 각 CartItem별 처리
        for (CartItem cartItem : cart.getCartItems()) {
            Menu menu = cartItem.getMenu();
            Style style = cartItem.getStyle();
            
            // 아이템 기본 가격 계산 (메뉴 가격 + 스타일 추가 가격)
            int basePrice = menu.getPrice() + style.getExtraPrice();
            
            // 옵션 처리
            List<OrderPreviewResponse.OptionResponse> optionResponses = new ArrayList<>();
            int optionPrice = 0;
            
            if (cartItem.getCartItemOptions() != null && !cartItem.getCartItemOptions().isEmpty()) {
                for (CartItemOption cartItemOption : cartItem.getCartItemOptions()) {
                    MenuOption menuOption = cartItemOption.getMenuOption();
                    
                    // 옵션 정보 추가
                    optionResponses.add(new OrderPreviewResponse.OptionResponse(
                            menuOption.getName(),
                            cartItemOption.getQuantity()
                    ));
                    
                    // 옵션 추가 가격 계산 (기본 수량을 초과하는 만큼만 계산)
                    optionPrice += menuOption.calculateExtraCost(cartItemOption.getQuantity());
                }
            }
            
            // 아이템 총 가격 계산 (기본 가격 + 옵션 가격) * 수량
            int price = (basePrice + optionPrice) * cartItem.getQuantity();
            itemsTotalPrice += price;
            
            // 아이템 응답 생성
            itemResponses.add(new OrderPreviewResponse.ItemResponse(
                    menu.getName(),
                    cartItem.getQuantity(),
                    price,
                    style.getName(),
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
                                    orderItem.getStyle().getId(),
                                    orderItem.getStyle().getName(),
                                    orderItem.getOrderItemOptions().stream().map(opt ->
                                            new OrderListResponse.OrderSummary.OptionSummaryDto(
                                                    opt.getMenuOption().getId(),
                                                    opt.getMenuOption().getName(),
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
                                    orderItem.getStyle().getId(),
                                    orderItem.getStyle().getName(),
                                    orderItem.getOrderItemOptions().stream().map(opt ->
                                            new DeliveryOrderListResponse.OrderSummary.OptionSummaryDto(
                                                    opt.getMenuOption().getId(),
                                                    opt.getMenuOption().getName(),
                                                    opt.getQuantity()
                                            )
                                    ).collect(java.util.stream.Collectors.toList())
                            ))
                            .collect(Collectors.toList());
                    
                    return new DeliveryOrderListResponse.OrderSummary(
                            order.getId(),
                            order.getDeliveryStatus().toString(),
                            order.getDeliveryTime().toString(),
                            order.getAddress(),
                            orderItems
                    );
                })
                .collect(Collectors.toList());
        
        DeliveryOrderListResponse response = new DeliveryOrderListResponse(orderList);
        
        return ResponseEntity.ok(response);
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
                                if (orderItem.getStyle() != null) {
                                    itemMap.put("styleName", orderItem.getStyle().getName());
                                }
                                
                                // 옵션 목록
                                List<Map<String, Object>> optionsList = orderItem.getOrderItemOptions().stream()
                                        .map(option -> {
                                            Map<String, Object> optionMap = new HashMap<>();
                                            optionMap.put("name", option.getMenuOption().getName());
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
                // 요리 완료 시 재고 확인 및 차감
                try {
                    // 주문에 포함된 모든 옵션의 재고 확인
                    for (OrderItem orderItem : order.getOrderItems()) {
                        for (OrderItemOption orderItemOption : orderItem.getOrderItemOptions()) {
                            MenuOption menuOption = orderItemOption.getMenuOption();
                            int quantity = orderItemOption.getQuantity();
                            storageService.checkStock(menuOption, quantity);
                        }
                    }
                    
                    // 재고 확인 성공 시 재고 차감
                    for (OrderItem orderItem : order.getOrderItems()) {
                        for (OrderItemOption orderItemOption : orderItem.getOrderItemOptions()) {
                            MenuOption menuOption = orderItemOption.getMenuOption();
                            int quantity = orderItemOption.getQuantity();
                            storageService.deductStock(menuOption, quantity);
                        }
                    }
                    
                    // 재고 차감 성공 시 주문 상태 변경
                    order.completeCooking();
                } catch (IllegalStateException e) {
                    // 재고 부족 시 409 Conflict 에러 반환
                    throw new IllegalStateException("재고가 부족하여 요리를 완료할 수 없습니다: " + e.getMessage());
                }
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
                                option.getMenuOption().getName(),
                                option.getQuantity(),
                                option.getMenuOption().getPrice()
                        );
                options.add(optionDetail);
            }
            
            OrderStatusResponse.OrderItemDetail itemDetail = 
                    new OrderStatusResponse.OrderItemDetail(
                            orderItem.getMenu().getId(),
                            orderItem.getMenu().getName(),
                            orderItem.getQuantity(),
                            orderItem.getMenu().getPrice(),
                            orderItem.getStyle() != null ? orderItem.getStyle().getId() : null,
                            orderItem.getStyle() != null ? orderItem.getStyle().getName() : null,
                            orderItem.getStyle() != null ? orderItem.getStyle().getExtraPrice() : 0,
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

}


