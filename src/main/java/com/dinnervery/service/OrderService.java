package com.dinnervery.service;

import com.dinnervery.dto.order.response.OrderResponse;
import com.dinnervery.dto.order.response.OrderListResponse;
import com.dinnervery.dto.order.response.DeliveryOrderListResponse;
import com.dinnervery.dto.order.response.OrderUpdateResponse;
import com.dinnervery.dto.order.request.OrderCreateRequest;
import com.dinnervery.entity.*;
import com.dinnervery.repository.CartRepository;
import com.dinnervery.repository.CustomerRepository;
import com.dinnervery.repository.OrderRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final CartRepository cartRepository;
    private final StorageService storageService;

    @Transactional
    public OrderResponse createOrder(OrderCreateRequest request) {
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new IllegalArgumentException("고객을 찾을 수 없습니다: " + request.getCustomerId()));

        Cart cart = cartRepository.findByCustomer_Id(request.getCustomerId())
                .orElseThrow(() -> new IllegalArgumentException("장바구니가 비어있습니다. 주문할 상품을 장바구니에 담아주세요."));

        if (cart.getCartItems() == null || cart.getCartItems().isEmpty()) {
            throw new IllegalArgumentException("장바구니가 비어있습니다. 주문할 상품을 장바구니에 담아주세요.");
        }

        Order order = Order.builder()
                .customer(customer)
                .address(request.getAddress())
                .cardNumber(request.getCardNumber())
                .deliveryTime(request.getDeliveryTime())
                .build();

        for (CartItem cartItem : cart.getCartItems()) {
            OrderItem orderItem = OrderItem.builder()
                    .menu(cartItem.getMenu())
                    .style(cartItem.getStyle())
                    .quantity(cartItem.getQuantity())
                    .build();

            for (CartItemOption cartItemOption : cartItem.getCartItemOptions()) {
                OrderItemOption orderItemOption = OrderItemOption.builder()
                        .menuOption(cartItemOption.getMenuOption())
                        .quantity(cartItemOption.getQuantity())
                        .build();
                orderItem.addOrderItemOption(orderItemOption);
            }

            order.addOrderItem(orderItem);
        }

        Order savedOrder = orderRepository.save(order);

        cartRepository.delete(cart);
        
        return OrderResponse.from(savedOrder);
    }

    public OrderResponse getOrderById(Long id) {
        Order order = orderRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다: " + id));
        return OrderResponse.from(order);
    }

    public List<OrderResponse> getOrdersByCustomerId(Long customerId) {
        List<Order> orders = orderRepository.findByCustomerIdWithDetails(customerId);
        return orders.stream()
                .map(OrderResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public OrderResponse completeOrder(Long orderId) {
        Order order = orderRepository.findByIdWithDetails(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다: " + orderId));

        if (order.getDeliveryStatus() == Order.Status.REQUESTED) {
            order.startCooking();
            order.completeCooking();
            order.startDelivering();
            order.completeDelivering();
        }
        
        Order completedOrder = orderRepository.save(order);
        
        Customer customer = completedOrder.getCustomer();
        customer.incrementOrderCount();
        customerRepository.save(customer);
        
        return OrderResponse.from(completedOrder);
    }

    public OrderListResponse getCookingOrders() {
        List<Order> orders = orderRepository.findByDeliveryStatusInWithDetails(List.of(Order.Status.REQUESTED, Order.Status.COOKING));

        List<OrderListResponse.OrderSummary> orderList = orders.stream()
                .map(order -> {
                    List<OrderListResponse.OrderSummary.OrderedItem> orderItems = 
                            (order.getOrderItems() != null && !order.getOrderItems().isEmpty())
                            ? order.getOrderItems().stream()
                            .map(orderItem -> {
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
        
        return new OrderListResponse(orderList);
    }

    public DeliveryOrderListResponse getDeliveryOrders() {
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
        
        return new DeliveryOrderListResponse(orderList);
    }

    public Map<String, Object> getOrdersByCustomerIdForResponse(Long customerId) {
        List<Order> orders = orderRepository.findByCustomerIdWithDetails(customerId);
        
        List<Map<String, Object>> orderList = orders.stream()
                .map(order -> {
                    Map<String, Object> orderMap = new HashMap<>();
                    orderMap.put("orderId", order.getId());
                    
                    String orderDate = order.getCreatedAt().toLocalDate().toString()
                            .replace("-", ".");
                    orderMap.put("orderDate", orderDate);
                    
                    orderMap.put("totalPrice", order.getTotalPrice());
                    orderMap.put("status", order.getDeliveryStatus().toString());
                    orderMap.put("deliveryTime", order.getDeliveryTime().toString());
                    
                    List<Map<String, Object>> orderItemsList = order.getOrderItems().stream()
                            .map(orderItem -> {
                                Map<String, Object> itemMap = new HashMap<>();
                                itemMap.put("name", orderItem.getMenu().getName());
                                itemMap.put("quantity", orderItem.getQuantity());
                                
                                if (orderItem.getStyle() != null) {
                                    itemMap.put("styleName", orderItem.getStyle().getName());
                                }
                                
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
        
        return response;
    }

    @Transactional
    public OrderUpdateResponse updateOrderStatus(Long id, Map<String, Object> request) {
        Order order = orderRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다: " + id));
        
        String newStatus = (String) request.get("status");
        if (newStatus == null || newStatus.isBlank()) {
            throw new IllegalArgumentException("상태 값이 필요합니다.");
        }
        
        switch (newStatus) {
            case "COOKING":
                order.startCooking();
                break;
            case "COOKED":
                try {
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
                                        storageService.checkStock(menuOption, quantity);
                                    }
                                }
                            }
                        }
                    }
                    
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
                                        storageService.deductStock(menuOption, quantity);
                                    }
                                }
                            }
                        }
                    }
                    
                    order.completeCooking();
                } catch (IllegalStateException e) {
                    throw new IllegalStateException("재고가 부족하여 요리를 완료할 수 없습니다: " + e.getMessage());
                }
                break;
            case "DELIVERING":
                order.startDelivering();
                break;
            case "DONE":
                order.completeDelivering();
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
        
        return new OrderUpdateResponse(
                savedOrder.getId(),
                savedOrder.getDeliveryStatus().toString()
        );
    }
}


