package com.dinnervery.service;

import com.dinnervery.dto.order.response.OrderResponse;
import com.dinnervery.dto.order.request.OrderCreateRequest;
import com.dinnervery.entity.*;
import com.dinnervery.repository.CartRepository;
import com.dinnervery.repository.CustomerRepository;
import com.dinnervery.repository.OrderRepository;

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
    private final CartRepository cartRepository;

    @Transactional
    public OrderResponse createOrder(OrderCreateRequest request) {
        // 고객 조회
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new IllegalArgumentException("고객을 찾을 수 없습니다: " + request.getCustomerId()));

        // 장바구니 조회
        Cart cart = cartRepository.findByCustomer_Id(request.getCustomerId())
                .orElseThrow(() -> new IllegalArgumentException("장바구니가 비어있습니다. 주문할 상품을 장바구니에 담아주세요."));

        // 장바구니가 비어있는지 확인
        if (cart.getCartItems() == null || cart.getCartItems().isEmpty()) {
            throw new IllegalArgumentException("장바구니가 비어있습니다. 주문할 상품을 장바구니에 담아주세요.");
        }

        // 주문 생성
        Order order = Order.builder()
                .customer(customer)
                .address(request.getAddress())
                .cardNumber(request.getCardNumber())
                .deliveryTime(request.getDeliveryTime())
                .build();

        // 장바구니 아이템들을 주문 아이템으로 복사
        for (CartItem cartItem : cart.getCartItems()) {
            // OrderItem 생성 (CartItem에서 복사)
            OrderItem orderItem = OrderItem.builder()
                    .menu(cartItem.getMenu())
                    .style(cartItem.getStyle())
                    .quantity(cartItem.getQuantity())
                    .build();

            // CartItemOption들을 OrderItemOption으로 복사
            for (CartItemOption cartItemOption : cartItem.getCartItemOptions()) {
                OrderItemOption orderItemOption = OrderItemOption.builder()
                        .menuOption(cartItemOption.getMenuOption())
                        .quantity(cartItemOption.getQuantity())
                        .build();
                orderItem.addOrderItemOption(orderItemOption);
            }

            order.addOrderItem(orderItem);
        }

        // 주문 저장
        Order savedOrder = orderRepository.save(order);

        // 장바구니 비우기 (CartItem들이 cascade로 삭제됨)
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


