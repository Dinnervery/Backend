package com.dinnervery.order;

import com.dinnervery.dto.request.OrderCreateRequest;
import com.dinnervery.dto.request.OrderItemCreateRequest;
import com.dinnervery.entity.Customer;
import com.dinnervery.entity.Menu;
import com.dinnervery.entity.ServingStyle;
import com.dinnervery.repository.CustomerRepository;
import com.dinnervery.repository.MenuRepository;
import com.dinnervery.repository.ServingStyleRepository;
import com.dinnervery.service.PriceCalculator;

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
class PriceCalculatorTest {

    @Mock
    private MenuRepository menuRepository;

    @Mock
    private ServingStyleRepository servingStyleRepository;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private PriceCalculator priceCalculator;

    private Menu champagneMenu;
    private ServingStyle grandStyle;
    private Customer customer;

    @BeforeEach
    void setUp() {
        // 샴페인 축제 디너 메뉴 설정
        champagneMenu = Menu.builder()
                .name("샴페인 축제 디너")
                .price(90000)
                .description("프리미엄 샴페인과 함께하는 축제 디너")
                .build();

        // 서빙 스타일 설정
        grandStyle = ServingStyle.builder()
                .name("그랜드 스타일")
                .extraPrice(10000)
                .build();

        // 고객 설정
        customer = Customer.builder()
                .loginId("test_customer")
                .password("password")
                .name("테스트 고객")
                .phoneNumber("010-1234-5678")
                .build();
    }

    @Test
    void testChampagneFestivalDinnerPrice() {
        // Given
        OrderItemCreateRequest itemRequest = OrderItemCreateRequest.builder()
                .menuId(1L)
                .servingStyleId(1L)
                .quantity(1)
                .build();

        OrderCreateRequest request = OrderCreateRequest.builder()
                .customerId(1L)
                .orderItems(List.of(itemRequest))
                .build();

        when(menuRepository.findById(1L)).thenReturn(Optional.of(champagneMenu));
        when(servingStyleRepository.findById(1L)).thenReturn(Optional.of(grandStyle));
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

        // When
        int totalPrice = priceCalculator.calcOrderTotal(request);

        // Then
        assertThat(totalPrice).isEqualTo(100000); // 90000 + 10000
    }
}
