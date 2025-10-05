package com.dinnervery.backend.order;

import com.dinnervery.backend.dto.request.OrderCreateRequest;
import com.dinnervery.backend.dto.request.OrderItemCreateRequest;
import com.dinnervery.backend.entity.Customer;
import com.dinnervery.backend.entity.Menu;
import com.dinnervery.backend.entity.ServingStyle;
import com.dinnervery.backend.repository.CustomerRepository;
import com.dinnervery.backend.repository.MenuRepository;
import com.dinnervery.backend.repository.ServingStyleRepository;
import com.dinnervery.backend.service.PriceCalculator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VipDiscountRuleTest {

    @Mock
    private MenuRepository menuRepository;

    @Mock
    private ServingStyleRepository servingStyleRepository;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private PriceCalculator priceCalculator;

    private Menu testMenu;
    private ServingStyle simpleStyle;

    @BeforeEach
    void setUp() {
        // 테스트 메뉴 설정
        testMenu = Menu.builder()
                .name("샴페인 축제 디너")
                .price(90000)
                .description("프리미엄 샴페인과 함께하는 축제 디너")
                .build();

        // SIMPLE 서빙 스타일 설정
        simpleStyle = ServingStyle.builder()
                .name("SIMPLE")
                .extraPrice(0)
                .build();
    }

    @Test
    void 고객_주문횟수_14회_이번_15번째_주문_미할인() {
        // Given
        Customer customer = Customer.builder()
                .loginId("test123")
                .password("password123")
                .name("테스트 고객")
                .phoneNumber("01012345678")
                .build();
        
        // 주문 횟수를 14회로 설정
        for (int i = 0; i < 14; i++) {
            customer.incrementOrderCount();
        }

        OrderItemCreateRequest itemRequest = OrderItemCreateRequest.builder()
                .menuId(1L)
                .servingStyleId(1L)
                .quantity(1)
                .build();

        OrderCreateRequest request = OrderCreateRequest.builder()
                .customerId(1L)
                .addressId(1L)
                .orderItems(List.of(itemRequest))
                .build();

        when(menuRepository.findById(1L)).thenReturn(Optional.of(testMenu));
        when(servingStyleRepository.findById(1L)).thenReturn(Optional.of(simpleStyle));
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

        // When
        int totalPrice = priceCalculator.calcOrderTotal(request);

        // Then
        // 주문 횟수 9회이므로 아직 VIP 할인 대상이 아님 (10회 이상이어야 함)
        // 총 가격 90,000원 (할인 없음)
        assertThat(totalPrice).isEqualTo(90000);
    }

    @Test
    void 고객_주문횟수_15회_이번_16번째_주문_10퍼센트_할인() {
        // Given
        Customer customer = Customer.builder()
                .loginId("test123")
                .password("password123")
                .name("테스트 고객")
                .phoneNumber("01012345678")
                .build();
        
        // 주문 횟수를 15회로 설정
        for (int i = 0; i < 15; i++) {
            customer.incrementOrderCount();
        }

        OrderItemCreateRequest itemRequest = OrderItemCreateRequest.builder()
                .menuId(1L)
                .servingStyleId(1L)
                .quantity(1)
                .build();

        OrderCreateRequest request = OrderCreateRequest.builder()
                .customerId(1L)
                .addressId(1L)
                .orderItems(List.of(itemRequest))
                .build();

        when(menuRepository.findById(1L)).thenReturn(Optional.of(testMenu));
        when(servingStyleRepository.findById(1L)).thenReturn(Optional.of(simpleStyle));
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

        // When
        int totalPrice = priceCalculator.calcOrderTotal(request);

        // Then
        // 주문 횟수 15회이고 이번이 16번째 주문이므로 VIP 할인 적용
        // 총 가격 90,000원 * 0.9 = 81,000원 (소수점 버림)
        assertThat(totalPrice).isEqualTo(81000);
    }

    @Test
    void 고객_주문횟수_21회_이번_22번째_주문_10퍼센트_할인() {
        // Given
        Customer customer = Customer.builder()
                .loginId("test@example.com")
                .password("password123")
                .name("테스트 고객")
                .phoneNumber("01012345678")
                .build();
        
        // 주문 횟수를 21회로 설정
        for (int i = 0; i < 21; i++) {
            customer.incrementOrderCount();
        }

        OrderItemCreateRequest itemRequest = OrderItemCreateRequest.builder()
                .menuId(1L)
                .servingStyleId(1L)
                .quantity(1)
                .build();

        OrderCreateRequest request = OrderCreateRequest.builder()
                .customerId(1L)
                .addressId(1L)
                .orderItems(List.of(itemRequest))
                .build();

        when(menuRepository.findById(1L)).thenReturn(Optional.of(testMenu));
        when(servingStyleRepository.findById(1L)).thenReturn(Optional.of(simpleStyle));
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

        // When
        int totalPrice = priceCalculator.calcOrderTotal(request);

        // Then
        // 주문 횟수 21회이고 이번이 22번째 주문이므로 VIP 할인 적용
        // 총 가격 90,000원 * 0.9 = 81,000원 (소수점 버림)
        assertThat(totalPrice).isEqualTo(81000);
    }

    @Test
    void 고객_주문횟수_11회_이번_12번째_주문_미할인() {
        // Given
        Customer customer = Customer.builder()
                .loginId("test@example.com")
                .password("password123")
                .name("테스트 고객")
                .phoneNumber("01012345678")
                .build();
        
        // 주문 횟수를 11회로 설정
        for (int i = 0; i < 11; i++) {
            customer.incrementOrderCount();
        }

        OrderItemCreateRequest itemRequest = OrderItemCreateRequest.builder()
                .menuId(1L)
                .servingStyleId(1L)
                .quantity(1)
                .build();

        OrderCreateRequest request = OrderCreateRequest.builder()
                .customerId(1L)
                .addressId(1L)
                .orderItems(List.of(itemRequest))
                .build();

        when(menuRepository.findById(1L)).thenReturn(Optional.of(testMenu));
        when(servingStyleRepository.findById(1L)).thenReturn(Optional.of(simpleStyle));
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

        // When
        int totalPrice = priceCalculator.calcOrderTotal(request);

        // Then
        // 주문 횟수 11회이고 이번이 12번째 주문이므로 VIP 할인 미적용
        // VIP 할인은 11번째 주문마다 적용되므로 (11, 22, 33...)
        // 총 가격 90,000원 (할인 없음)
        assertThat(totalPrice).isEqualTo(90000);
    }
}


