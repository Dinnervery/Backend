package com.dinnervery.backend.order;

import com.dinnervery.backend.controller.OrderController;
import com.dinnervery.backend.dto.request.ReorderRequest;
import com.dinnervery.backend.entity.Address;
import com.dinnervery.backend.entity.Customer;
import com.dinnervery.backend.entity.Menu;
import com.dinnervery.backend.entity.Order;
import com.dinnervery.backend.entity.OrderItem;
import com.dinnervery.backend.entity.ServingStyle;
import com.dinnervery.backend.repository.AddressRepository;
import com.dinnervery.backend.repository.CustomerRepository;
import com.dinnervery.backend.repository.MenuRepository;
import com.dinnervery.backend.repository.OrderRepository;
import com.dinnervery.backend.repository.ServingStyleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
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
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ServingStyleRepository servingStyleRepository;

    private Customer customer;
    private Menu menu1;
    private Menu menu2;
    private Address address1;
    private Address address2;
    private ServingStyle servingStyle;
    private Order originalOrder;

    @BeforeEach
    void setUp() {
        // SQL을 사용한 강제 테이블 정리 (외래키 제약조건 무시)
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY FALSE");
        jdbcTemplate.execute("TRUNCATE TABLE addresses");
        jdbcTemplate.execute("TRUNCATE TABLE orders");
        jdbcTemplate.execute("TRUNCATE TABLE customers");
        jdbcTemplate.execute("TRUNCATE TABLE menus");
        jdbcTemplate.execute("TRUNCATE TABLE serving_styles");
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY TRUE");
        
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
                .name("잉글리시 디너")
                .price(35000)
                .description("영국 전통 디너 코스")
                .build();
        menu2 = menuRepository.save(menu2);

        // 서빙 스타일 생성
        servingStyle = ServingStyle.builder()
                .name("일반")
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
                .build();
        originalOrder = orderRepository.save(originalOrder);

        // 주문 항목 추가
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
    void 재주문_API_테스트() {
        // 재주문 요청 생성
        ReorderRequest reorderRequest = ReorderRequest.builder()
                .customerId(customer.getId())
                .addressId(address2.getId()) // 다른 주소로 재주문
                .cardNumber("9876-5432-1098-7654")
                .build();
        
        // 재주문 API 호출
        ResponseEntity<com.dinnervery.backend.dto.order.OrderResponse> response = orderController.reorder(originalOrder.getId(), reorderRequest);
        
        // 응답 검증
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        com.dinnervery.backend.dto.order.OrderResponse responseBody = response.getBody();
        assertThat(responseBody).isNotNull();
        
        // orderItems 검증
        List<com.dinnervery.backend.dto.order.OrderItemResponse> orderItems = responseBody.getOrderItems();
        assertThat(orderItems).hasSize(2);
        
        // 첫 번째 주문 항목 검증 (발렌타인 디너)
        com.dinnervery.backend.dto.order.OrderItemResponse firstItem = orderItems.stream()
                .filter(item -> item.getMenuId().equals(menu1.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(firstItem.getMenuId()).isEqualTo(menu1.getId());
        assertThat(firstItem.getQuantity()).isEqualTo(2);
        assertThat(firstItem.getServingStyle().getStyleId()).isEqualTo(servingStyle.getId());
        
        // 두 번째 주문 항목 검증 (잉글리시 디너)
        com.dinnervery.backend.dto.order.OrderItemResponse secondItem = orderItems.stream()
                .filter(item -> item.getMenuId().equals(menu2.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(secondItem.getMenuId()).isEqualTo(menu2.getId());
        assertThat(secondItem.getQuantity()).isEqualTo(1);
        assertThat(secondItem.getServingStyle().getStyleId()).isEqualTo(servingStyle.getId());
    }

    @Test
    void 존재하지_않는_주문_재주문_예외_테스트() {
        // 재주문 요청 생성
        ReorderRequest reorderRequest = ReorderRequest.builder()
                .customerId(customer.getId())
                .addressId(address1.getId())
                .cardNumber("1234-5678-9012-3456")
                .build();
        
        // 존재하지 않는 주문 ID로 재주문 시도
        assertThatThrownBy(() -> orderController.reorder(999L, reorderRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("원본 주문을 찾을 수 없습니다");
    }
}
