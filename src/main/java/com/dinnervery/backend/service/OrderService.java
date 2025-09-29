package com.dinnervery.backend.service;

import com.dinnervery.backend.dto.OrderDto;
import com.dinnervery.backend.dto.request.OrderCreateRequest;
import com.dinnervery.backend.dto.request.OrderItemCreateRequest;
import com.dinnervery.backend.dto.request.ReorderRequest;
import com.dinnervery.backend.entity.*;
import com.dinnervery.backend.repository.AddressRepository;
import com.dinnervery.backend.repository.CustomerRepository;
import com.dinnervery.backend.repository.MenuRepository;
import com.dinnervery.backend.repository.OrderRepository;

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

    @Transactional
    public OrderDto createOrder(OrderCreateRequest request) {
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
                .build();

        // 주문 항목들 추가
        for (OrderItemCreateRequest itemRequest : request.getOrderItems()) {
            Menu menu = menuRepository.findById(itemRequest.getMenuId())
                    .orElseThrow(() -> new IllegalArgumentException("메뉴를 찾을 수 없습니다: " + itemRequest.getMenuId()));

            OrderItem orderItem = OrderItem.builder()
                    .menu(menu)
                    .orderedQty(itemRequest.getOrderedQty())
                    .build();

            order.addOrderItem(orderItem);
        }

        Order savedOrder = orderRepository.save(order);
        return OrderDto.from(savedOrder);
    }

    public OrderDto getOrderById(Long id) {
        Order order = orderRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다: " + id));
        return OrderDto.from(order);
    }

    public List<OrderDto> getOrdersByCustomerId(Long customerId) {
        List<Order> orders = orderRepository.findByCustomerIdWithDetails(customerId);
        return orders.stream()
                .map(OrderDto::from)
                .collect(Collectors.toList());
    }

    public List<OrderDto> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(OrderDto::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public OrderDto completeOrder(Long orderId) {
        Order order = orderRepository.findByIdWithDetails(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다: " + orderId));

        // 주문 상태를 단계별로 전이
        if (order.getDeliveryStatus() == Order.status.REQUESTED) {
            order.startCooking();
        }
        if (order.getDeliveryStatus() == Order.status.COOKING) {
            order.startDelivering();
        }
        if (order.getDeliveryStatus() == Order.status.DELIVERING) {
            order.completeDelivery();
        }
        
        Order completedOrder = orderRepository.save(order);
        
        // 고객 주문수 증가 및 등급 갱신 (트랜잭션 내에서 보장)
        Customer customer = completedOrder.getCustomer();
        customer.incrementOrderCount();
        customerRepository.save(customer);
        
        return OrderDto.from(completedOrder);
    }

    @Transactional
    public void cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다: " + orderId));
        
        orderRepository.delete(order);
    }

    @Transactional
    public OrderDto reorder(Long originalOrderId, ReorderRequest request) {
        // 원본 주문 조회
        Order originalOrder = orderRepository.findByIdWithDetails(originalOrderId)
                .orElseThrow(() -> new IllegalArgumentException("원본 주문을 찾을 수 없습니다: " + originalOrderId));

        // 고객 조회
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new IllegalArgumentException("고객을 찾을 수 없습니다: " + request.getCustomerId()));

        // 주소 조회
        Address address = addressRepository.findById(request.getAddressId())
                .orElseThrow(() -> new IllegalArgumentException("주소를 찾을 수 없습니다: " + request.getAddressId()));

        // 새 주문 생성
        Order newOrder = Order.builder()
                .customer(customer)
                .address(address)
                .cardNumber(request.getCardNumber())
                .build();

        // 원본 주문의 주문 항목들을 복제
        for (OrderItem originalItem : originalOrder.getOrderItems()) {
            OrderItem newOrderItem = OrderItem.builder()
                    .menu(originalItem.getMenu())
                    .servingStyle(originalItem.getServingStyle())
                    .orderedQty(originalItem.getOrderedQty())
                    .build();

            newOrder.addOrderItem(newOrderItem);
        }

        Order savedOrder = orderRepository.save(newOrder);
        return OrderDto.from(savedOrder);
    }
}


