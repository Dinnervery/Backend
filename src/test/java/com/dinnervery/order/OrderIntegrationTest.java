package com.dinnervery.order;

import com.dinnervery.controller.OrderController;
import com.dinnervery.dto.request.PriceCalculationRequest;
import com.dinnervery.entity.Address;
import com.dinnervery.entity.Customer;
import com.dinnervery.repository.AddressRepository;
import com.dinnervery.repository.CustomerRepository;
import com.dinnervery.entity.Menu;
import com.dinnervery.repository.MenuRepository;
import com.dinnervery.repository.OrderRepository;
import com.dinnervery.repository.ServingStyleRepository;
import com.dinnervery.entity.Order;
import com.dinnervery.entity.OrderItem;
import com.dinnervery.entity.ServingStyle;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class OrderIntegrationTest {

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

    private Customer testCustomer;
    private Menu testMenu;
    private Menu testMenuEntity;
    private ServingStyle testServingStyle;

    @BeforeEach
    void setUp() {
        // given - 테스트 데이터 생성
        // 테스트 고객 생성
        testCustomer = Customer.builder()
                .loginId("test123_" + System.currentTimeMillis())
                .password("password123")
                .name("테스트 고객")
                .phoneNumber("01012345678")
                .build();
        testCustomer = customerRepository.save(testCustomer);
        
        // 주문 수를 5개로 설정
        for (int i = 0; i < 5; i++) {
            testCustomer.incrementOrderCount();
        }
        testCustomer = customerRepository.save(testCustomer);

        // 테스트 메뉴 생성
        testMenu = Menu.builder()
                .name("발렌타인 디너")
                .price(28000)
                .description("로맨틱한 발렌타인 특별 디너")
                .build();
        testMenu = menuRepository.save(testMenu);

        // MenuOption을 위한 별도의 Menu 생성
        testMenuEntity = Menu.builder()
                .name("프랑스식 디너")
                .price(35000)
                .description("프랑스 전통 디너 코스")
                .build();
        testMenuEntity = menuRepository.save(testMenuEntity);

        // 테스트용 서빙 스타일 생성
        testServingStyle = ServingStyle.builder()
                .name("TEST_STYLE")
                .extraPrice(5000)
                .build();
        testServingStyle = servingStyleRepository.save(testServingStyle);
    }

    @Test
    @org.junit.jupiter.api.Order(1)
    void testPriceCalculationAPI() {
        // given - 간단한 가격계산 요청 생성
        PriceCalculationRequest.OrderItemRequest orderItem = PriceCalculationRequest.OrderItemRequest.builder()
                .menuId(testMenu.getId())
                .quantity(1)
                .servingStyleId(testServingStyle.getId())
                .optionIds(List.of())
                .build();
        
        PriceCalculationRequest request = PriceCalculationRequest.builder()
                .customerId(testCustomer.getId())
                .items(List.of(orderItem))
                .build();
        
        // when - 가격계산 API 호출
        ResponseEntity<com.dinnervery.dto.response.PriceCalculationResponse> response = orderController.calculatePrice(request);
        
        // then - 응답 검증
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        com.dinnervery.dto.response.PriceCalculationResponse responseBody = response.getBody();
        assertThat(responseBody).isNotNull();
        
        // 기본적인 가격 정보 검증
        if (responseBody != null) {
            assertThat(responseBody.getSubtotal()).isNotNull();
            assertThat(responseBody.getFinalPrice()).isNotNull();
            assertThat(responseBody.getCustomerGrade()).isEqualTo("BASIC");
        }
    }
    

    @Test
    @org.junit.jupiter.api.Order(2)
    void testOrderCreationWithValidatedItems() {
        // given - 주소 생성
        Address testAddress = Address.builder()
                .customer(testCustomer)
                .address("서울시 강남구 테헤란로 123")
                .build();
        testAddress = addressRepository.save(testAddress);

        // 주문 생성
        Order order = Order.builder()
                .customer(testCustomer)
                .address(testAddress)
                .cardNumber("1234-5678-9012-3456")
                .deliveryTime(LocalTime.of(20, 0))
                .build();
        order = orderRepository.save(order);

        // 주문 아이템 생성
        OrderItem orderItem = OrderItem.builder()
                .menu(testMenu)
                .servingStyle(testServingStyle)
                .quantity(1)
                .build();
        order.addOrderItem(orderItem);
        order = orderRepository.save(order);

        // then - 기본적인 검증
        assertThat(order.getId()).isNotNull();
        assertThat(order.getCustomer().getId()).isEqualTo(testCustomer.getId());
        assertThat(order.getOrderItems()).hasSize(1);
    }

    @Test
    @org.junit.jupiter.api.Order(3)
    void testOrderRetrieval() {
        // given - 주소 생성
        Address testAddress = Address.builder()
                .customer(testCustomer)
                .address("서울시 강남구 테헤란로 123")
                .build();
        testAddress = addressRepository.save(testAddress);

        // 주문 생성
        Order order = Order.builder()
                .customer(testCustomer)
                .address(testAddress)
                .cardNumber("1234-5678-9012-3456")
                .deliveryTime(LocalTime.of(20, 0))
                .build();
        order = orderRepository.save(order);

        OrderItem orderItem = OrderItem.builder()
                .menu(testMenu)
                .servingStyle(testServingStyle)
                .quantity(1)
                .build();
        order.addOrderItem(orderItem);
        order = orderRepository.save(order);

        // when - 주문 조회
        Optional<Order> foundOrder = orderRepository.findByIdWithDetails(order.getId());

        // then - 기본적인 검증
        assertThat(foundOrder).isPresent();
        assertThat(foundOrder.get().getId()).isEqualTo(order.getId());
        assertThat(foundOrder.get().getCustomer().getId()).isEqualTo(testCustomer.getId());
    }
}
