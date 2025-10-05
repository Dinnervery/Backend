package com.dinnervery.backend.order;

import com.dinnervery.backend.controller.OrderController;
import com.dinnervery.backend.dto.request.PriceCalculationRequest;
import com.dinnervery.backend.dto.request.OrderItemCreateRequest;
import com.dinnervery.backend.entity.Address;
import com.dinnervery.backend.entity.Customer;
import com.dinnervery.backend.repository.AddressRepository;
import com.dinnervery.backend.repository.CustomerRepository;
import com.dinnervery.backend.entity.Menu;
import com.dinnervery.backend.repository.MenuRepository;
import com.dinnervery.backend.repository.OrderItemRepository;
import com.dinnervery.backend.repository.OrderRepository;
import com.dinnervery.backend.repository.ServingStyleRepository;
import com.dinnervery.backend.entity.Order;
import com.dinnervery.backend.entity.OrderItem;
import com.dinnervery.backend.entity.ServingStyle;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class OrderIntegrationTest {

    @Autowired
    private OrderController orderController;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

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
        // DB 초기화
        addressRepository.deleteAll();
        customerRepository.deleteAll();
        
        // 테스트 고객 생성
        testCustomer = Customer.builder()
                .loginId("test123_" + UUID.randomUUID())
                .password("password123")
                .name("테스트 고객")
                .phoneNumber("01012345678")
                .build();
        testCustomer = customerRepository.save(testCustomer);
        
        // 주문 횟수를 5회로 설정
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
                .name("잉글리시 디너")
                .price(35000)
                .description("영국 전통 디너 코스")
                .build();
        testMenuEntity = menuRepository.save(testMenuEntity);

        // 테스트 서빙 스타일 생성
        testServingStyle = ServingStyle.builder()
                .name("TEST_STYLE")
                .extraPrice(5000)
                .build();
        testServingStyle = servingStyleRepository.save(testServingStyle);
    }

    @Test
    void 가격_계산_API_테스트() {
        // 가격 계산 요청 생성
        PriceCalculationRequest.OrderItemRequest orderItem = PriceCalculationRequest.OrderItemRequest.builder()
                .menuId(testMenu.getId())
                .quantity(2)
                .servingStyleId(testServingStyle.getId())
                .optionIds(List.of())
                .build();
        
        PriceCalculationRequest request = PriceCalculationRequest.builder()
                .customerId(testCustomer.getId())
                .items(List.of(orderItem))
                .build();
        
        // 가격 계산 API 호출
        ResponseEntity<Map<String, Object>> response = orderController.calculatePrice(request);
        
        // 응답 검증
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        Map<String, Object> responseBody = response.getBody();
        assertThat(responseBody).isNotNull();
        
        // 가격 정보 검증
        assertThat(responseBody.get("subtotal")).isNotNull();
        assertThat(responseBody.get("finalPrice")).isNotNull();
        assertThat(responseBody.get("customerGrade")).isEqualTo("BASIC");
        assertThat(responseBody.get("discountRate")).isEqualTo(0);
        
        // 할인 전 가격 계산: 메뉴(28000) + 서빙스타일(5000) = 33000 * 2 = 66000
        int expectedSubtotal = (testMenu.getPrice() + testServingStyle.getExtraPrice()) * 2;
        assertThat(responseBody.get("subtotal")).isEqualTo(expectedSubtotal);
        assertThat(responseBody.get("finalPrice")).isEqualTo(expectedSubtotal); // BASIC 등급이므로 할인 없음
    }
    
    @Test
    void VIP_고객_가격_계산_API_테스트() {
        // 고객을 VIP로 만들기 위해 주문수 15회로 설정
        for (int i = 0; i < 15; i++) {
            testCustomer.incrementOrderCount();
        }
        testCustomer = customerRepository.save(testCustomer);
        
        // 가격 계산 요청 생성
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
        
        // 가격 계산 API 호출
        ResponseEntity<Map<String, Object>> response = orderController.calculatePrice(request);
        
        // 응답 검증
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        Map<String, Object> responseBody = response.getBody();
        assertThat(responseBody).isNotNull();
        
        // VIP 할인 검증
        assertThat(responseBody.get("customerGrade")).isEqualTo("VIP");
        assertThat(responseBody.get("discountRate")).isEqualTo(10);
        
        // 할인 전 가격: 메뉴(28000) + 서빙스타일(5000) = 33000
        int expectedSubtotal = testMenu.getPrice() + testServingStyle.getExtraPrice();
        int expectedDiscount = (int) (expectedSubtotal * 0.1);
        int expectedFinalPrice = expectedSubtotal - expectedDiscount;
        
        assertThat(responseBody.get("subtotal")).isEqualTo(expectedSubtotal);
        assertThat(responseBody.get("discountAmount")).isEqualTo(expectedDiscount);
        assertThat(responseBody.get("finalPrice")).isEqualTo(expectedFinalPrice);
    }

    @Test
    void 주문_생성_후_저장된_데이터_검증() {
        // Given
        OrderItemCreateRequest itemRequest = OrderItemCreateRequest.builder()
                .menuId(testMenu.getId())
                .servingStyleId(testServingStyle.getId())
                .quantity(1)
                .build();

        // 주소 생성
        Address         testAddress = Address.builder()
                .customer(testCustomer)
                .address("서울시 강남구 테헤란로 123")
                .build();
        testAddress = addressRepository.save(testAddress);

        // 주문 생성
        Order order = Order.builder()
                .customer(testCustomer)
                .address(testAddress)
                .cardNumber("1234-5678-9012-3456")
                .build();
        orderRepository.save(order);

        // 주문 항목 생성
        OrderItem orderItem = OrderItem.builder()
                .menu(testMenu)
                .servingStyle(testServingStyle)
                .quantity(itemRequest.getQuantity())
                .build();
        order.addOrderItem(orderItem);
        OrderItem savedOrderItem = orderItemRepository.save(orderItem);

        // 고객 주문 횟수 증가
        testCustomer.incrementOrderCount();
        Customer updatedCustomer = customerRepository.save(testCustomer);

        // Then
        // 저장된 주문 검증
        assertThat(order.getId()).isNotNull();
        assertThat(order.getCustomer().getId()).isEqualTo(testCustomer.getId());
        assertThat(order.getTotalPrice()).isNotNull();
        assertThat(order.getFinalPrice()).isNotNull();

        // 저장된 주문 항목 검증
        assertThat(savedOrderItem.getId()).isNotNull();
        assertThat(savedOrderItem.getOrder().getId()).isEqualTo(order.getId());
        assertThat(savedOrderItem.getMenu().getId()).isEqualTo(testMenu.getId());
        assertThat(savedOrderItem.getQuantity()).isEqualTo(1);

        // 고객 주문 횟수 증가 검증
        assertThat(updatedCustomer.getOrderCount()).isEqualTo(6); // 5 -> 6으로 증가

        // 고객 등급 변경 검증 (6번째 주문이므로 아직 BASIC)
        assertThat(updatedCustomer.getGrade()).isEqualTo(Customer.CustomerGrade.BASIC);
    }

    @Test
    void 고객_주문횟수_10회_달성시_VIP_등급_변경() {
        // Given
        // 고객의 주문 횟수를 9회로 설정 (이미 setUp에서 5회로 설정되어 있으므로 4회만 추가)
        testCustomer = customerRepository.findById(testCustomer.getId()).orElseThrow();
        for (int i = 0; i < 4; i++) {
            testCustomer.incrementOrderCount();
        }
        testCustomer = customerRepository.save(testCustomer);

        OrderItemCreateRequest itemRequest = OrderItemCreateRequest.builder()
                .menuId(testMenu.getId())
                .servingStyleId(testServingStyle.getId())
                .quantity(1)
                .build();

        // 주소 생성
        Address         testAddress = Address.builder()
                .customer(testCustomer)
                .address("서울시 강남구 테헤란로 123")
                .build();
        testAddress = addressRepository.save(testAddress);

        // When
        // 주문 생성
        Order order = Order.builder()
                .customer(testCustomer)
                .address(testAddress)
                .cardNumber("1234-5678-9012-3456")
                .build();
        orderRepository.save(order);

        // 주문 항목 생성
        OrderItem orderItem = OrderItem.builder()
                .menu(testMenu)
                .servingStyle(testServingStyle)
                .quantity(itemRequest.getQuantity())
                .build();
        order.addOrderItem(orderItem);
        orderItemRepository.save(orderItem);

        // 고객 주문 횟수 증가 (15번째 주문)
        testCustomer.incrementOrderCount();
        Customer updatedCustomer = customerRepository.save(testCustomer);

        // Then
        // 고객 주문 횟수 검증
        assertThat(updatedCustomer.getOrderCount()).isEqualTo(10);

        // 고객 등급 VIP 변경 검증
        assertThat(updatedCustomer.getGrade()).isEqualTo(Customer.CustomerGrade.VIP);
    }

    @Test
    void 주문_조회_테스트() {
        // 주소 생성
        Address         testAddress = Address.builder()
                .customer(testCustomer)
                .address("서울시 강남구 테헤란로 123")
                .build();
        testAddress = addressRepository.save(testAddress);

        // Given
        Order order = Order.builder()
                .customer(testCustomer)
                .address(testAddress)
                .cardNumber("1234-5678-9012-3456")
                .build();
        orderRepository.save(order);

        OrderItem orderItem = OrderItem.builder()
                .menu(testMenu)
                .servingStyle(testServingStyle)
                .quantity(1)
                .build();
        order.addOrderItem(orderItem);
        orderItemRepository.save(orderItem);

        // When
        Optional<Order> foundOrder = orderRepository.findByIdWithDetails(order.getId());

        // Then
        assertThat(foundOrder).isPresent();
        assertThat(foundOrder.get().getId()).isEqualTo(order.getId());
        assertThat(foundOrder.get().getCustomer().getId()).isEqualTo(testCustomer.getId());
        assertThat(foundOrder.get().getOrderItems()).hasSize(1);
        assertThat(foundOrder.get().getOrderItems().get(0).getMenu().getId()).isEqualTo(testMenu.getId());
    }
}
