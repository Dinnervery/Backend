package com.dinnervery.service;

import com.dinnervery.dto.order.OrderResponse;
import com.dinnervery.dto.request.OrderCreateRequest;
import com.dinnervery.dto.request.OrderItemCreateRequest;
import com.dinnervery.entity.*;
import com.dinnervery.repository.AddressRepository;
import com.dinnervery.repository.CustomerRepository;
import com.dinnervery.repository.MenuRepository;
import com.dinnervery.repository.MenuOptionRepository;
import com.dinnervery.repository.OrderRepository;
import com.dinnervery.repository.ServingStyleRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final MenuRepository menuRepository;
    private final AddressRepository addressRepository;
    private final ServingStyleRepository servingStyleRepository;
    private final MenuOptionRepository menuOptionRepository;
    private final StorageService storageService;

    @Transactional
    public OrderResponse createOrder(OrderCreateRequest request) {
        // 고객 조회
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new IllegalArgumentException("고객을 찾을 수 없습니다: " + request.getCustomerId()));

        // 주소 조회
        Address address = addressRepository.findById(request.getAddressId())
                .orElseThrow(() -> new IllegalArgumentException("주소를 찾을 수 없습니다: " + request.getAddressId()));

        // 주문 생성
        Order order = Order.builder()
                .customer(customer)
                .address(address)
                .cardNumber(request.getCardNumber())
                .deliveryTime(request.getDeliveryTime())
                .build();

        // 재고 확인 (save 이전)
        if (request.getOrderItems() != null) {
            for (OrderItemCreateRequest itemRequest : request.getOrderItems()) {
                if (itemRequest.getOptions() != null) {
                    for (OrderItemCreateRequest.OptionRequest optReq : itemRequest.getOptions()) {
                        MenuOption menuOption = menuOptionRepository.findById(optReq.getOptionId())
                                .orElseThrow(() -> new IllegalArgumentException("옵션을 찾을 수 없습니다: " + optReq.getOptionId()));
                        storageService.checkStock(menuOption, optReq.getQuantity());
                    }
                }
            }
        }

        // 주문 아이템들 추가
        for (OrderItemCreateRequest itemRequest : request.getOrderItems()) {
            Menu menu = menuRepository.findById(itemRequest.getMenuId())
                    .orElseThrow(() -> new IllegalArgumentException("메뉴를 찾을 수 없습니다: " + itemRequest.getMenuId()));
            
            ServingStyle servingStyle = servingStyleRepository.findById(itemRequest.getStyleId())
                    .orElseThrow(() -> new IllegalArgumentException("서빙 스타일을 찾을 수 없습니다: " + itemRequest.getStyleId()));

            OrderItem orderItem = OrderItem.builder()
                    .menu(menu)
                    .servingStyle(servingStyle)
                    .quantity(itemRequest.getQuantity())
                    .build();

            // 옵션 매핑
            if (itemRequest.getOptions() != null && !itemRequest.getOptions().isEmpty()) {
                for (OrderItemCreateRequest.OptionRequest optReq : itemRequest.getOptions()) {
                    MenuOption menuOption = menuOptionRepository.findById(optReq.getOptionId())
                            .orElseThrow(() -> new IllegalArgumentException("옵션을 찾을 수 없습니다: " + optReq.getOptionId()));
                    OrderItemOption orderItemOption = OrderItemOption.builder()
                            .menuOption(menuOption)
                            .quantity(optReq.getQuantity())
                            .build();
                    orderItem.addOrderItemOption(orderItemOption);
                }
            }

            order.addOrderItem(orderItem);
        }

        Order savedOrder = orderRepository.save(order);

        // 재고 차감 (save 이후)
        if (request.getOrderItems() != null) {
            for (OrderItemCreateRequest itemRequest : request.getOrderItems()) {
                if (itemRequest.getOptions() != null) {
                    for (OrderItemCreateRequest.OptionRequest optReq : itemRequest.getOptions()) {
                        MenuOption menuOption = menuOptionRepository.findById(optReq.getOptionId())
                                .orElseThrow(() -> new IllegalArgumentException("옵션을 찾을 수 없습니다: " + optReq.getOptionId()));
                        storageService.deductStock(menuOption, optReq.getQuantity());
                    }
                }
            }
        }
        
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

        // 주문을 완료 상태로 변경
        if (order.getDeliveryStatus() == Order.Status.REQUESTED) {
            order.startCooking();
            order.completeCooking();
            order.startDelivering();
            order.completeDelivery();
        }
        
        Order completedOrder = orderRepository.save(order);
        
        // 고객 주문수 증가 및 등급 갱신 (트랜잭션 내에서 보장)
        Customer customer = completedOrder.getCustomer();
        customer.incrementOrderCount();
        customerRepository.save(customer);
        
        return OrderResponse.from(completedOrder);
    }
}


