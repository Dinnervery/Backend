package com.dinnervery.backend.order;

import com.dinnervery.backend.dto.order.OrderCreateRequest;
import com.dinnervery.backend.dto.order.OrderItemOptionRequest;
import com.dinnervery.backend.dto.order.OrderItemRequest;
import com.dinnervery.backend.entity.Customer;
import com.dinnervery.backend.repository.CustomerRepository;
import com.dinnervery.backend.repository.MenuOptionRepository;
import com.dinnervery.backend.entity.Menu;
import com.dinnervery.backend.entity.MenuOption;
import com.dinnervery.backend.entity.ServingStyle;
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
class PriceCalculatorTest {

    @Mock
    private MenuRepository menuRepository;

    @Mock
    private MenuOptionRepository menuOptionRepository;

    @Mock
    private ServingStyleRepository servingStyleRepository;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private PriceCalculator priceCalculator;

    private Menu champagneMenu;
    private MenuOption champagneOption;
    private ServingStyle simpleStyle;
    private ServingStyle grandStyle;
    private ServingStyle deluxeStyle;
    private Customer customer;

    @BeforeEach
    void setUp() {
        // 샴페인 축제 디너 메뉴 설정
        champagneMenu = Menu.builder()
                .name("샴페인 축제 디너")
                .price(90000)
                .description("프리미엄 샴페인과 함께하는 축제 디너")
                .build();

        // 샴페인 옵션 설정 (기본 2병)
        champagneOption = MenuOption.builder()
                .menu(champagneMenu)
                .itemName("샴페인")
                .itemPrice(25000)
                .defaultQty(2) // 기본 2병
                .build();

        // 서빙 스타일 설정
        simpleStyle = ServingStyle.builder()
                .name("SIMPLE")
                .extraPrice(0)
                .build();

        grandStyle = ServingStyle.builder()
                .name("GRAND")
                .extraPrice(5000)
                .build();

        deluxeStyle = ServingStyle.builder()
                .name("DELUXE")
                .extraPrice(10000)
                .build();

        // 고객 설정
        customer = Customer.builder()
                .loginId("test123")
                .password("password123")
                .name("테스트 고객")
                .phoneNumber("01012345678")
                .build();
    }


    @Test
    void 샴페인_디너_GRAND_스타일_추가요금_반영() {
        // Given
        OrderItemRequest itemRequest = OrderItemRequest.builder()
                .menuId(1L)
                .servingStyleId(2L) // GRAND 스타일
                .orderedQty(1)
                .options(List.of())
                .build();

        OrderCreateRequest request = OrderCreateRequest.builder()
                .customerId(1L)
                .addressId(1L)
                .items(List.of(itemRequest))
                .build();

        when(menuRepository.findById(1L)).thenReturn(Optional.of(champagneMenu));
        when(servingStyleRepository.findById(2L)).thenReturn(Optional.of(grandStyle));
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

        // When
        int totalPrice = priceCalculator.calcOrderTotal(request);

        // Then
        // 샴페인 축제 디너 기본 가격 90,000 + GRAND 스타일 추가비 5,000 = 95,000원
        assertThat(totalPrice).isEqualTo(95000);
    }

    @Test
    void 샴페인_디너_DELUXE_스타일_추가요금_반영() {
        // Given
        OrderItemRequest itemRequest = OrderItemRequest.builder()
                .menuId(1L)
                .servingStyleId(3L) // DELUXE 스타일
                .orderedQty(1)
                .options(List.of())
                .build();

        OrderCreateRequest request = OrderCreateRequest.builder()
                .customerId(1L)
                .addressId(1L)
                .items(List.of(itemRequest))
                .build();

        when(menuRepository.findById(1L)).thenReturn(Optional.of(champagneMenu));
        when(servingStyleRepository.findById(3L)).thenReturn(Optional.of(deluxeStyle));
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

        // When
        int totalPrice = priceCalculator.calcOrderTotal(request);

        // Then
        // 샴페인 축제 디너 기본 가격 90,000 + DELUXE 스타일 추가비 10,000 = 100,000원
        assertThat(totalPrice).isEqualTo(100000);
    }

    @Test
    void 샴페인_디너_샴페인_3병_주문시_옵션_델타_계산() {
        // Given
        OrderItemOptionRequest optionRequest = OrderItemOptionRequest.builder()
                .menuOptionId(1L)
                .orderedQty(3) // 기본 2병에서 3병으로 설정 (1병 추가)
                .build();

        OrderItemRequest itemRequest = OrderItemRequest.builder()
                .menuId(1L)
                .servingStyleId(1L)
                .orderedQty(1)
                .options(List.of(optionRequest))
                .build();

        OrderCreateRequest request = OrderCreateRequest.builder()
                .customerId(1L)
                .addressId(1L)
                .items(List.of(itemRequest))
                .build();

        when(menuRepository.findById(1L)).thenReturn(Optional.of(champagneMenu));
        when(servingStyleRepository.findById(1L)).thenReturn(Optional.of(simpleStyle));
        when(menuOptionRepository.findById(1L)).thenReturn(Optional.of(champagneOption));
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

        // When
        int totalPrice = priceCalculator.calcOrderTotal(request);

        // Then
        // 기본 가격 90,000 + 샴페인 추가 1병(25,000) = 115,000원
        assertThat(totalPrice).isEqualTo(115000);
    }
}
