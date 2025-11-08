package com.dinnervery.controller;

import com.dinnervery.dto.order.response.OrderListResponse;
import com.dinnervery.dto.order.response.DeliveryOrderListResponse;
import com.dinnervery.dto.order.response.OrderUpdateResponse;
import com.dinnervery.entity.Customer;
import com.dinnervery.entity.MenuOption;
import com.dinnervery.entity.Order;
import com.dinnervery.entity.OrderItem;
import com.dinnervery.entity.OrderItemOption;
import com.dinnervery.repository.CustomerRepository;
import com.dinnervery.repository.OrderRepository;
import com.dinnervery.service.StorageService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class OrderController {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final StorageService storageService;

    // 조리 대기목록 조회 (요리직원용)
    @GetMapping("/orders/cooking")
    public ResponseEntity<OrderListResponse> getCookingOrders() {
        List<Order> orders = orderRepository.findByDeliveryStatusInWithDetails(List.of(Order.Status.REQUESTED, Order.Status.COOKING));

        List<OrderListResponse.OrderSummary> orderList = orders.stream()
                .map(order -> {
                    List<OrderListResponse.OrderSummary.OrderedItem> orderItems = 
                            (order.getOrderItems() != null && !order.getOrderItems().isEmpty())
                            ? order.getOrderItems().stream()
                            .map(orderItem -> {
                                // null 체크 추가
                                if (orderItem.getMenu() == null || orderItem.getStyle() == null) {
                                    throw new IllegalStateException("주문 항목에 메뉴 또는 스타일 정보가 없습니다. 주문 ID: " + order.getId());
                                }
                                
                                List<OrderListResponse.OrderSummary.OptionSummaryDto> options = 
                                    (orderItem.getOrderItemOptions() != null && !orderItem.getOrderItemOptions().isEmpty()) 
                                        ? orderItem.getOrderItemOptions().stream()
                                            .filter(opt -> opt.getMenuOption() != null)
                                            .map(opt -> new OrderListResponse.OrderSummary.OptionSummaryDto(
                                                    opt.getMenuOption().getId(),
                                                    opt.getMenuOption().getName(),
                                                    opt.getQuantity()
                                            ))
                                            .collect(java.util.stream.Collectors.toList())
                                        : java.util.Collections.emptyList();
                                
                                return new OrderListResponse.OrderSummary.OrderedItem(
                                        orderItem.getMenu().getId(),
                                        orderItem.getMenu().getName(),
                                        orderItem.getQuantity(),
                                        orderItem.getStyle().getId(),
                                        orderItem.getStyle().getName(),
                                        options
                                );
                            })
                            .collect(Collectors.toList())
                            : java.util.Collections.emptyList();
                    
                    // deliveryTime null 체크 추가
                    String deliveryTimeStr = order.getDeliveryTime() != null 
                            ? order.getDeliveryTime().toString() 
                            : "";
                    
                    return new OrderListResponse.OrderSummary(
                            order.getId(),
                            order.getDeliveryStatus() != null ? order.getDeliveryStatus().toString() : "UNKNOWN",
                            deliveryTimeStr,
                            orderItems
                    );
                })
                .collect(Collectors.toList());
        
        OrderListResponse response = new OrderListResponse(orderList);
        
        return ResponseEntity.ok(response);
    }
    
    // 배달 대기목록 조회 (배달직원용)
    @GetMapping("/orders/delivery")
    public ResponseEntity<DeliveryOrderListResponse> getDeliveryOrders() {
        List<Order> orders = orderRepository.findByDeliveryStatusInWithDetails(List.of(Order.Status.COOKED, Order.Status.DELIVERING));

        List<DeliveryOrderListResponse.OrderSummary> orderList = orders.stream()
                .map(order -> {
                    List<DeliveryOrderListResponse.OrderSummary.OrderItem> orderItems = order.getOrderItems().stream()
                            .map(orderItem -> {
                                List<DeliveryOrderListResponse.OrderSummary.OptionSummaryDto> options = 
                                    (orderItem.getOrderItemOptions() != null && !orderItem.getOrderItemOptions().isEmpty()) 
                                        ? orderItem.getOrderItemOptions().stream()
                                            .filter(opt -> opt.getMenuOption() != null)
                                            .map(opt -> new DeliveryOrderListResponse.OrderSummary.OptionSummaryDto(
                                                    opt.getMenuOption().getId(),
                                                    opt.getMenuOption().getName(),
                                                    opt.getQuantity()
                                            ))
                                            .collect(java.util.stream.Collectors.toList())
                                        : java.util.Collections.emptyList();
                                
                                return new DeliveryOrderListResponse.OrderSummary.OrderItem(
                                        orderItem.getMenu().getId(),
                                        orderItem.getMenu().getName(),
                                        orderItem.getQuantity(),
                                        orderItem.getStyle().getId(),
                                        orderItem.getStyle().getName(),
                                        options
                                );
                            })
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
                                List<Map<String, Object>> optionsList = 
                                    (orderItem.getOrderItemOptions() != null && !orderItem.getOrderItemOptions().isEmpty())
                                        ? orderItem.getOrderItemOptions().stream()
                                            .filter(option -> option.getMenuOption() != null)
                                            .map(option -> {
                                                Map<String, Object> optionMap = new HashMap<>();
                                                optionMap.put("name", option.getMenuOption().getName());
                                                optionMap.put("quantity", option.getQuantity());
                                                return optionMap;
                                            })
                                            .collect(Collectors.toList())
                                        : java.util.Collections.emptyList();
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

    // 통합된 주문 상태 변경 API
    @PatchMapping("/orders/{id}/status")
    @Transactional
    public ResponseEntity<OrderUpdateResponse> updateOrderStatus(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        Order order = orderRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다: " + id));
        
        String newStatus = (String) request.get("status");
        if (newStatus == null || newStatus.isBlank()) {
            throw new IllegalArgumentException("상태 값이 필요합니다.");
        }
        
        // 상태별 다른 처리
        switch (newStatus) {
            case "COOKING":
                order.startCooking();
                break;
            case "COOKED":
                // 요리 완료 시 재고 확인 및 차감
                try {
                    // 주문에 포함된 모든 옵션의 재고 확인 및 차감
                    if (order.getOrderItems() != null) {
                        for (OrderItem orderItem : order.getOrderItems()) {
                            // 옵션이 없는 주문 항목도 있을 수 있으므로 체크
                            if (orderItem.getOrderItemOptions() != null && !orderItem.getOrderItemOptions().isEmpty()) {
                                for (OrderItemOption orderItemOption : orderItem.getOrderItemOptions()) {
                                    MenuOption menuOption = orderItemOption.getMenuOption();
                                    if (menuOption != null) {
                                        Integer quantity = orderItemOption.getQuantity();
                                        if (quantity == null || quantity < 1) {
                                            throw new IllegalStateException("옵션 수량이 유효하지 않습니다. 주문 ID: " + id);
                                        }
                                        // 재고 확인
                                        storageService.checkStock(menuOption, quantity);
                                    }
                                }
                            }
                        }
                    }
                    
                    // 재고 확인 성공 시 재고 차감
                    if (order.getOrderItems() != null) {
                        for (OrderItem orderItem : order.getOrderItems()) {
                            if (orderItem.getOrderItemOptions() != null && !orderItem.getOrderItemOptions().isEmpty()) {
                                for (OrderItemOption orderItemOption : orderItem.getOrderItemOptions()) {
                                    MenuOption menuOption = orderItemOption.getMenuOption();
                                    if (menuOption != null) {
                                        Integer quantity = orderItemOption.getQuantity();
                                        if (quantity == null || quantity < 1) {
                                            throw new IllegalStateException("옵션 수량이 유효하지 않습니다. 주문 ID: " + id);
                                        }
                                        // 재고 차감
                                        storageService.deductStock(menuOption, quantity);
                                    }
                                }
                            }
                        }
                    }
                    
                    // 재고 차감 성공 시 주문 상태 변경
                    order.completeCooking();
                } catch (IllegalStateException e) {
                    // 재고 부족 시 예외 재발생 (상태 코드는 @ControllerAdvice에서 처리)
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
                if (customer == null) {
                    throw new IllegalStateException("주문에 고객 정보가 없습니다. 주문 ID: " + id);
                }
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

}


