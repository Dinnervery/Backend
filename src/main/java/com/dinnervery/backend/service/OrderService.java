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
import com.dinnervery.backend.repository.ServingStyleRepository;

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
    private final BusinessHoursService businessHoursService;

    @Transactional
    public OrderDto createOrder(OrderCreateRequest request) {
        // 라스트오더 시간 검증 (21:30 이후 주문 시도 시 410 에러)
        if (businessHoursService.isAfterLastOrderTime()) {
            throw new IllegalStateException("마감되었습니다.");
        }
        
        // 영업시간 검증 (영업시간 외 주문 시도 시 503 에러)
        if (!businessHoursService.isBusinessHours()) {
            throw new IllegalStateException("현재 영업시간이 아닙니다. 영업시간: 오후 5시 ~ 오후 11시");
        }

        // 배송 희망 시간 검증
        if (!businessHoursService.isValidDeliveryTime(request.getDeliveryTime())) {
            throw new IllegalArgumentException("배송 희망 시간이 유효하지 않습니다. 16:00-22:00 사이의 10분 단위 시간을 선택해주세요.");
        }

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

        // 주문 항목들 추가
        for (OrderItemCreateRequest itemRequest : request.getOrderItems()) {
            Menu menu = menuRepository.findById(itemRequest.getMenuId())
                    .orElseThrow(() -> new IllegalArgumentException("메뉴를 찾을 수 없습니다: " + itemRequest.getMenuId()));
            
            ServingStyle servingStyle = servingStyleRepository.findById(itemRequest.getServingStyleId())
                    .orElseThrow(() -> new IllegalArgumentException("서빙 스타일을 찾을 수 없습니다: " + itemRequest.getServingStyleId()));

            OrderItem orderItem = OrderItem.builder()
                    .menu(menu)
                    .servingStyle(servingStyle)
                    .quantity(itemRequest.getQuantity())
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
    public OrderDto reorder(Long originalOrderId, ReorderRequest request) {
        // 라스트오더 시간 검증 (21:30 이후 주문 시도 시 410 에러)
        if (businessHoursService.isAfterLastOrderTime()) {
            throw new IllegalStateException("마감되었습니다.");
        }
        
        // 영업시간 검증 (영업시간 외 주문 시도 시 503 에러)
        if (!businessHoursService.isBusinessHours()) {
            throw new IllegalStateException("현재 영업시간이 아닙니다. 영업시간: 오후 5시 ~ 오후 11시");
        }

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
                    .quantity(originalItem.getQuantity())
                    .build();

            newOrder.addOrderItem(newOrderItem);
        }

        Order savedOrder = orderRepository.save(newOrder);
        
        return OrderDto.from(savedOrder);
    }
}


