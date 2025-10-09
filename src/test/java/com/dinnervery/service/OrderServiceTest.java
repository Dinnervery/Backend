package com.dinnervery.service;

import com.dinnervery.dto.OrderDto;
import com.dinnervery.dto.request.OrderCreateRequest;
import com.dinnervery.dto.request.OrderItemCreateRequest;
import com.dinnervery.dto.request.ReorderRequest;
import com.dinnervery.entity.*;
import com.dinnervery.repository.AddressRepository;
import com.dinnervery.repository.CustomerRepository;
import com.dinnervery.repository.MenuRepository;
import com.dinnervery.repository.OrderRepository;
import com.dinnervery.repository.ServingStyleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.List;
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
    private MenuRepository menuRepository;

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private ServingStyleRepository servingStyleRepository;

    @Mock
    private BusinessHoursService businessHoursService;

    @InjectMocks
    private OrderService orderService;

    private Customer testCustomer;
    private Address testAddress;
    private Menu testMenu;
    private ServingStyle testServingStyle;
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

        testAddress = Address.builder()
                .customer(testCustomer)
                .address("서울시 강남구 테헤란로 123")
                .build();

        testMenu = Menu.builder()
                .name("발렌타인 디너")
                .price(28000)
                .description("로맨틱한 발렌타인 특별 디너")
                .build();

        testServingStyle = ServingStyle.builder()
                .name("기본")
                .extraPrice(0)
                .build();

        testOrder = Order.builder()
                .customer(testCustomer)
                .address(testAddress)
                .cardNumber("1234-5678-9012-3456")
                .deliveryTime(LocalTime.of(20, 0))
                .build();
    }

    @Test
    void createOrder_성공() {
        // given - 주문 생성 요청
        OrderItemCreateRequest orderItemRequest = OrderItemCreateRequest.builder()
                .menuId(1L)
                .servingStyleId(1L)
                .quantity(2)
                .build();

        OrderCreateRequest request = OrderCreateRequest.builder()
                .customerId(1L)
                .addressId(1L)
                .cardNumber("1234-5678-9012-3456")
                .deliveryTime(LocalTime.of(20, 0))
                .orderItems(List.of(orderItemRequest))
                .build();

        // Mock 설정
        when(businessHoursService.isAfterLastOrderTime()).thenReturn(false);
        when(businessHoursService.isBusinessHours()).thenReturn(true);
        when(businessHoursService.isValidDeliveryTime(any(LocalTime.class))).thenReturn(true);
        when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
        when(addressRepository.findById(1L)).thenReturn(Optional.of(testAddress));
        when(menuRepository.findById(1L)).thenReturn(Optional.of(testMenu));
        when(servingStyleRepository.findById(1L)).thenReturn(Optional.of(testServingStyle));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        // when - 주문 생성
        OrderDto result = orderService.createOrder(request);

        // then - 결과 검증
        assertThat(result).isNotNull();

        // Mock 호출 검증
        verify(businessHoursService).isAfterLastOrderTime();
        verify(businessHoursService).isBusinessHours();
        verify(businessHoursService).isValidDeliveryTime(LocalTime.of(20, 0));
        verify(customerRepository).findById(1L);
        verify(addressRepository).findById(1L);
        verify(menuRepository).findById(1L);
        verify(servingStyleRepository).findById(1L);
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void createOrder_마감시간_초과_예외() {
        // given - 주문 생성 요청
        OrderItemCreateRequest orderItemRequest = OrderItemCreateRequest.builder()
                .menuId(1L)
                .servingStyleId(1L)
                .quantity(2)
                .build();

        OrderCreateRequest request = OrderCreateRequest.builder()
                .customerId(1L)
                .addressId(1L)
                .cardNumber("1234-5678-9012-3456")
                .deliveryTime(LocalTime.of(20, 0))
                .orderItems(List.of(orderItemRequest))
                .build();

        // Mock 설정 - 마감시간 초과
        when(businessHoursService.isAfterLastOrderTime()).thenReturn(true);

        // when & then - 마감시간 초과 예외 발생
        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("마감되었습니다");

        // Mock 호출 검증
        verify(businessHoursService).isAfterLastOrderTime();
        verifyNoMoreInteractions(customerRepository, addressRepository, menuRepository, servingStyleRepository, orderRepository);
    }

    @Test
    void createOrder_고객_없음_예외() {
        // given - 주문 생성 요청
        OrderItemCreateRequest orderItemRequest = OrderItemCreateRequest.builder()
                .menuId(1L)
                .servingStyleId(1L)
                .quantity(2)
                .build();

        OrderCreateRequest request = OrderCreateRequest.builder()
                .customerId(999L) // 존재하지 않는 고객 ID
                .addressId(1L)
                .cardNumber("1234-5678-9012-3456")
                .deliveryTime(LocalTime.of(20, 0))
                .orderItems(List.of(orderItemRequest))
                .build();

        // Mock 설정
        when(businessHoursService.isAfterLastOrderTime()).thenReturn(false);
        when(businessHoursService.isBusinessHours()).thenReturn(true);
        when(businessHoursService.isValidDeliveryTime(any(LocalTime.class))).thenReturn(true);
        when(customerRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then - 고객 없음 예외 발생
        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("고객을 찾을 수 없습니다");

        // Mock 호출 검증
        verify(customerRepository).findById(999L);
        verifyNoMoreInteractions(addressRepository, menuRepository, servingStyleRepository, orderRepository);
    }

    @Test
    void getOrderById_성공() {
        // given - Mock 설정
        when(orderRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(testOrder));

        // when - 주문 조회
        OrderDto result = orderService.getOrderById(1L);

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

    @Test
    void reorder_성공() {
        // given - 재주문 요청
        ReorderRequest reorderRequest = ReorderRequest.builder()
                .customerId(1L)
                .addressId(1L)
                .cardNumber("9876-5432-1098-7654")
                .build();

        // Mock 설정
        when(businessHoursService.isAfterLastOrderTime()).thenReturn(false);
        when(businessHoursService.isBusinessHours()).thenReturn(true);
        when(orderRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(testOrder));
        when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
        when(addressRepository.findById(1L)).thenReturn(Optional.of(testAddress));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        // when - 재주문
        OrderDto result = orderService.reorder(1L, reorderRequest);

        // then - 결과 검증
        assertThat(result).isNotNull();

        // Mock 호출 검증
        verify(businessHoursService).isAfterLastOrderTime();
        verify(businessHoursService).isBusinessHours();
        verify(orderRepository).findByIdWithDetails(1L);
        verify(customerRepository).findById(1L);
        verify(addressRepository).findById(1L);
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void reorder_원본주문_없음_예외() {
        // given - 재주문 요청
        ReorderRequest reorderRequest = ReorderRequest.builder()
                .customerId(1L)
                .addressId(1L)
                .cardNumber("9876-5432-1098-7654")
                .build();

        // Mock 설정
        when(businessHoursService.isAfterLastOrderTime()).thenReturn(false);
        when(businessHoursService.isBusinessHours()).thenReturn(true);
        when(orderRepository.findByIdWithDetails(999L)).thenReturn(Optional.empty());

        // when & then - 원본주문 없음 예외 발생
        assertThatThrownBy(() -> orderService.reorder(999L, reorderRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("원본 주문을 찾을 수 없습니다");

        // Mock 호출 검증
        verify(businessHoursService).isAfterLastOrderTime();
        verify(businessHoursService).isBusinessHours();
        verify(orderRepository).findByIdWithDetails(999L);
        verifyNoMoreInteractions(customerRepository, addressRepository);
    }
}
