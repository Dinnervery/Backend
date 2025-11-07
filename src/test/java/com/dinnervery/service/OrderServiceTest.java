package com.dinnervery.service;

import com.dinnervery.dto.order.response.OrderResponse;
import com.dinnervery.dto.order.request.OrderCreateRequest;
import com.dinnervery.entity.*;
import com.dinnervery.repository.CartRepository;
import com.dinnervery.repository.CustomerRepository;
import com.dinnervery.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CartRepository cartRepository;

    @InjectMocks
    private OrderService orderService;

    private Customer testCustomer;
    private Menu testMenu;
    private Style testStyle;
    private Order testOrder;

    @BeforeEach
    void setUp() {
        // given - 테스트 데이터 생성
        testCustomer = Customer.builder()
                .loginId("test_customer")
                .password("password")
                .name("테스트 고객")
                .phoneNumber("010-1234-5678")
                .build();

        testMenu = Menu.builder()
                .name("발렌타인 디너")
                .price(28000)
                .build();

        testStyle = Style.builder()
                .name("기본")
                .extraPrice(0)
                .build();

        testOrder = Order.builder()
                .customer(testCustomer)
                .address("서울시 강남구 테헤란로 123")
                .cardNumber("1234-5678-9012-3456")
                .deliveryTime(LocalTime.of(20, 0))
                .build();
    }

    @Test
    void createOrder_성공() {
        // given - 주문 생성 요청
        OrderCreateRequest request = OrderCreateRequest.builder()
                .customerId(1L)
                .address("서울시 강남구 테헤란로 123")
                .cardNumber("1234-5678-9012-3456")
                .deliveryTime(LocalTime.of(20, 0))
                .build();

        // Cart와 CartItem 모킹
        Cart cart = Cart.builder().customer(testCustomer).build();
        CartItem cartItem = CartItem.builder()
                .menu(testMenu)
                .style(testStyle)
                .quantity(2)
                .build();
        cart.addCartItem(cartItem);

        // Mock 설정
        when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
        when(cartRepository.findByCustomer_Id(1L)).thenReturn(Optional.of(cart));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        // when - 주문 생성
        OrderResponse result = orderService.createOrder(request);

        // then - 결과 검증
        assertThat(result).isNotNull();

        // Mock 호출 검증
        verify(customerRepository).findById(1L);
        verify(cartRepository).findByCustomer_Id(1L);
        verify(orderRepository).save(any(Order.class));
        verify(cartRepository).delete(any(Cart.class));
    }


    @Test
    void createOrder_고객_없음_예외() {
        // given - 주문 생성 요청
        OrderCreateRequest request = OrderCreateRequest.builder()
                .customerId(999L) // 존재하지 않는 고객 ID
                .address("서울시 강남구 테헤란로 123")
                .cardNumber("1234-5678-9012-3456")
                .deliveryTime(LocalTime.of(20, 0))
                .build();

        // Mock 설정
        when(customerRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then - 고객 없음 예외 발생
        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("고객을 찾을 수 없습니다");

        // Mock 호출 검증
        verify(customerRepository).findById(999L);
        verifyNoMoreInteractions(cartRepository, orderRepository);
    }

    @Test
    void getOrderById_성공() {
        // given - Mock 설정
        when(orderRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(testOrder));

        // when - 주문 조회
        OrderResponse result = orderService.getOrderById(1L);

        // then - 결과 검증
        assertThat(result).isNotNull();

        // Mock 호출 검증
        verify(orderRepository).findByIdWithDetails(1L);
    }

    @Test
    void getOrderById_주문_없음_예외() {
        // given - Mock 설정
        when(orderRepository.findByIdWithDetails(999L)).thenReturn(Optional.empty());

        // when & then - 주문 없음 예외 발생
        assertThatThrownBy(() -> orderService.getOrderById(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("주문을 찾을 수 없습니다");

        // Mock 호출 검증
        verify(orderRepository).findByIdWithDetails(999L);
    }

}
