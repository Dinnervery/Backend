package com.dinnervery.order;

import com.dinnervery.controller.OrderController;
import com.dinnervery.dto.request.ReorderRequest;
import com.dinnervery.entity.Address;
import com.dinnervery.entity.Customer;
import com.dinnervery.entity.Menu;
import com.dinnervery.entity.Order;
import com.dinnervery.entity.OrderItem;
import com.dinnervery.entity.ServingStyle;
import com.dinnervery.repository.AddressRepository;
import com.dinnervery.repository.CustomerRepository;
import com.dinnervery.repository.MenuRepository;
import com.dinnervery.repository.OrderRepository;
import com.dinnervery.repository.ServingStyleRepository;
import com.dinnervery.service.BusinessHoursService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ReorderTest {

    @Autowired
    private OrderController orderController;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private MenuRepository menuRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private ServingStyleRepository servingStyleRepository;

    @MockBean
    private BusinessHoursService businessHoursService;

    private Customer customer;
    private Menu menu1;
    private Menu menu2;
    private Address address1;
    private Address address2;
    private ServingStyle servingStyle;
    private Order originalOrder;

    @BeforeEach
    void setUp() {
        // Mock 설정 - 영업시간 검증 우회
        when(businessHoursService.isAfterLastOrderTime()).thenReturn(false);
        when(businessHoursService.isBusinessHours()).thenReturn(true);
        
        // given - 테스트 데이터 생성
        // 고객 생성
        customer = Customer.builder()
                .loginId("test_customer_" + UUID.randomUUID())
                .password("password")
                .name("테스트 고객")
                .phoneNumber("010-1234-5678")
                .build();
        customer = customerRepository.save(customer);

        // 메뉴 생성
        menu1 = Menu.builder()
                .name("발렌타인 디너")
                .price(28000)
                .description("로맨틱한 발렌타인 특별 디너")
                .build();
        menu1 = menuRepository.save(menu1);

        menu2 = Menu.builder()
                .name("프랑스식 디너")
                .price(35000)
                .description("프랑스 전통 디너 코스")
                .build();
        menu2 = menuRepository.save(menu2);

        // 서빙 스타일 생성
        servingStyle = ServingStyle.builder()
                .name("기본_" + System.currentTimeMillis())
                .extraPrice(0)
                .build();
        servingStyle = servingStyleRepository.save(servingStyle);

        // 주소 생성
        address1 = Address.builder()
                .customer(customer)
                .address("서울시 강남구 테헤란로 123")
                .build();
        address1 = addressRepository.save(address1);

        address2 = Address.builder()
                .customer(customer)
                .address("서울시 서초구 서초대로 456")
                .build();
        address2 = addressRepository.save(address2);

        // 원본 주문 생성
        originalOrder = Order.builder()
                .customer(customer)
                .address(address1)
                .cardNumber("1234-5678-9012-3456")
                .deliveryTime(LocalTime.of(20, 0))
                .build();
        originalOrder = orderRepository.save(originalOrder);

        // 주문 아이템 추가
        OrderItem orderItem1 = OrderItem.builder()
                .menu(menu1)
                .servingStyle(servingStyle)
                .quantity(2)
                .build();
        originalOrder.addOrderItem(orderItem1);

        OrderItem orderItem2 = OrderItem.builder()
                .menu(menu2)
                .servingStyle(servingStyle)
                .quantity(1)
                .build();
        originalOrder.addOrderItem(orderItem2);

        originalOrder = orderRepository.save(originalOrder);
    }

    @Test
    void testReorderAPI() {
        // given - 재주문 요청 생성
        ReorderRequest reorderRequest = ReorderRequest.builder()
                .customerId(customer.getId())
                .addressId(address2.getId()) // 다른 주소로 재주문
                .cardNumber("9876-5432-1098-7654")
                .build();
        
        // when - 재주문 API 호출
        ResponseEntity<com.dinnervery.dto.order.OrderResponse> response = orderController.reorder(originalOrder.getId(), reorderRequest);
        
        // then - 응답 검증
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        com.dinnervery.dto.order.OrderResponse responseBody = response.getBody();
        assertThat(responseBody).isNotNull();
        
        // orderItems 검증
        if (responseBody != null) {
            List<com.dinnervery.dto.order.OrderItemResponse> orderItems = responseBody.getOrderItems();
            if (orderItems != null) {
                assertThat(orderItems).hasSize(2);
                
                // 첫 번째 주문 아이템 검증(발렌타인 디너)
                com.dinnervery.dto.order.OrderItemResponse firstItem = orderItems.stream()
                        .filter(item -> item.getMenuId().equals(menu1.getId()))
                        .findFirst()
                        .orElseThrow();
                assertThat(firstItem.getMenuId()).isEqualTo(menu1.getId());
                assertThat(firstItem.getQuantity()).isEqualTo(2);
                assertThat(firstItem.getServingStyle().getStyleId()).isEqualTo(servingStyle.getId());
                
                // 두 번째 주문 아이템 검증(프랑스식 디너)
                com.dinnervery.dto.order.OrderItemResponse secondItem = orderItems.stream()
                        .filter(item -> item.getMenuId().equals(menu2.getId()))
                        .findFirst()
                        .orElseThrow();
                assertThat(secondItem.getMenuId()).isEqualTo(menu2.getId());
                assertThat(secondItem.getQuantity()).isEqualTo(1);
                assertThat(secondItem.getServingStyle().getStyleId()).isEqualTo(servingStyle.getId());
            }
        }
    }

    @Test
    void testReorderWithNonExistentOrderException() {
        // given - 재주문 요청 생성
        ReorderRequest reorderRequest = ReorderRequest.builder()
                .customerId(customer.getId())
                .addressId(address1.getId())
                .cardNumber("1234-5678-9012-3456")
                .build();
        
        // when & then - 존재하지 않는 주문 ID로 재주문 시도
        assertThatThrownBy(() -> orderController.reorder(999L, reorderRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("원본 주문을 찾을 수 없습니다");
    }
}
