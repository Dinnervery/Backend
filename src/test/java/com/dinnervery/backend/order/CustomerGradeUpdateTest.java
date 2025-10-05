package com.dinnervery.backend.order;

import com.dinnervery.backend.controller.MemberController;
import com.dinnervery.backend.controller.MenuController;
import com.dinnervery.backend.entity.Address;
import com.dinnervery.backend.entity.Customer;
import com.dinnervery.backend.entity.Order;
import com.dinnervery.backend.entity.OrderItem;
import com.dinnervery.backend.entity.ServingStyle;
import com.dinnervery.backend.entity.Menu;
import com.dinnervery.backend.repository.AddressRepository;
import com.dinnervery.backend.repository.CustomerRepository;
import com.dinnervery.backend.repository.OrderRepository;
import com.dinnervery.backend.repository.ServingStyleRepository;
import com.dinnervery.backend.repository.MenuRepository;
import com.dinnervery.backend.repository.MenuOptionRepository;
import com.dinnervery.backend.repository.CartRepository;
import com.dinnervery.backend.repository.CartItemRepository;
import com.dinnervery.backend.repository.OrderItemRepository;
import com.dinnervery.backend.repository.OrderItemOptionRepository;
import com.dinnervery.backend.repository.EmployeeRepository;
import com.dinnervery.backend.service.OrderService;
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

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CustomerGradeUpdateTest {

    @Autowired
    private MemberController memberController;

    @Autowired
    private MenuController menuController;

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CustomerRepository customerRepository;

    private Customer customer;
    private Address address;
    private Menu menu;
    private ServingStyle servingStyle;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private MenuRepository menuRepository;

    @Autowired
    private ServingStyleRepository servingStyleRepository;

    @Autowired
    private MenuOptionRepository menuOptionRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private OrderItemOptionRepository orderItemOptionRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        // SQL을 사용한 강제 테이블 정리 (외래키 제약조건 무시)
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY FALSE");
        jdbcTemplate.execute("TRUNCATE TABLE order_item_options");
        jdbcTemplate.execute("TRUNCATE TABLE order_items");
        jdbcTemplate.execute("TRUNCATE TABLE orders");
        jdbcTemplate.execute("TRUNCATE TABLE cart_items");
        jdbcTemplate.execute("TRUNCATE TABLE carts");
        jdbcTemplate.execute("TRUNCATE TABLE addresses");
        jdbcTemplate.execute("TRUNCATE TABLE customers");
        jdbcTemplate.execute("TRUNCATE TABLE menu_option");
        jdbcTemplate.execute("TRUNCATE TABLE menus");
        jdbcTemplate.execute("TRUNCATE TABLE serving_styles");
        jdbcTemplate.execute("TRUNCATE TABLE employees");
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY TRUE");
        
        // 고객 생성
        customer = Customer.builder()
                .loginId("test_customer_" + UUID.randomUUID())
                .password("password")
                .name("테스트 고객")
                .phoneNumber("010-1234-5678")
                .build();
        customer = customerRepository.save(customer);

        // 주소 생성
        address = Address.builder()
                .customer(customer)
                .address("서울시 강남구 테헤란로 123")
                .build();
        address = addressRepository.save(address);

        // 메뉴 생성
        menu = Menu.builder()
                .name("발렌타인 디너")
                .price(28000)
                .description("로맨틱한 발렌타인 특별 디너")
                .build();
        menu = menuRepository.save(menu);

        // 서빙 스타일 생성
        servingStyle = ServingStyle.builder()
                .name("심플 스타일")
                .extraPrice(0)
                .build();
        servingStyle = servingStyleRepository.save(servingStyle);
    }

    @Test
    @org.junit.jupiter.api.Order(2)
    void 주문_완료_시_고객_주문수_증가_및_등급_갱신_테스트() {
        // 초기 상태 확인
        assertThat(customer.getOrderCount()).isEqualTo(0);
        assertThat(customer.getGrade()).isEqualTo(Customer.CustomerGrade.BASIC);

        // 주문 생성 및 완료
        Order order = Order.builder()
                .customer(customer)
                .address(address)
                .cardNumber("1234-5678-9012-3456")
                .build();
        order = orderRepository.save(order);

        // 주문 항목 추가
        OrderItem orderItem = OrderItem.builder()
                .menu(menu)
                .servingStyle(servingStyle)
                .quantity(1)
                .build();
        order.addOrderItem(orderItem);
        order = orderRepository.save(order);

        // 주문 완료
        orderService.completeOrder(order.getId());

        // 고객 정보 다시 조회
        Customer updatedCustomer = customerRepository.findById(customer.getId()).orElseThrow();

        // 주문수 증가 확인
        assertThat(updatedCustomer.getOrderCount()).isEqualTo(1);
        assertThat(updatedCustomer.getGrade()).isEqualTo(Customer.CustomerGrade.BASIC);

        // 15번째 주문까지 완료하여 VIP 등급 달성
        for (int i = 2; i <= 15; i++) {
            Order newOrder = Order.builder()
                    .customer(customer)
                    .address(address)
                    .cardNumber("1234-5678-9012-3456")
                    .build();
            newOrder = orderRepository.save(newOrder);

            // 주문 항목 추가
            OrderItem newOrderItem = OrderItem.builder()
                    .menu(menu)
                    .servingStyle(servingStyle)
                    .quantity(1)
                    .build();
            newOrder.addOrderItem(newOrderItem);
            newOrder = orderRepository.save(newOrder);

            orderService.completeOrder(newOrder.getId());
        }

        // 최종 고객 정보 확인
        Customer finalCustomer = customerRepository.findById(customer.getId()).orElseThrow();
        assertThat(finalCustomer.getOrderCount()).isEqualTo(15);
        assertThat(finalCustomer.getGrade()).isEqualTo(Customer.CustomerGrade.VIP);
    }

    @Test
    @org.junit.jupiter.api.Order(4)
    void 고객_등급_갱신_메서드_테스트() {
        // 초기 상태
        assertThat(customer.getGrade()).isEqualTo(Customer.CustomerGrade.BASIC);

        // 주문수 5로 설정
        customer.incrementOrderCount();
        customer.incrementOrderCount();
        customer.incrementOrderCount();
        customer.incrementOrderCount();
        customer.incrementOrderCount();
        customer = customerRepository.save(customer);
        assertThat(customer.getOrderCount()).isEqualTo(5);
        assertThat(customer.getGrade()).isEqualTo(Customer.CustomerGrade.BASIC);

        // 주문수 15로 설정하여 VIP 등급 달성
        for (int i = 0; i < 10; i++) {
            customer.incrementOrderCount();
        }
        customer = customerRepository.save(customer);
        assertThat(customer.getOrderCount()).isEqualTo(15);
        assertThat(customer.getGrade()).isEqualTo(Customer.CustomerGrade.VIP);
    }


    @Test
    @org.junit.jupiter.api.Order(1)
    void 고객_정보_조회_API_테스트() {
        // 고객 정보 조회 API 호출
        ResponseEntity<Map<String, Object>> response = memberController.getCustomer(customer.getId());
        
        // 응답 검증
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        Map<String, Object> responseBody = response.getBody();
        assertThat(responseBody).isNotNull();
        
        // 고객 정보 검증
        assertThat(responseBody.get("customerId")).isEqualTo(customer.getId());
        assertThat(responseBody.get("loginId")).isEqualTo(customer.getLoginId());
        assertThat(responseBody.get("name")).isEqualTo(customer.getName());
        assertThat(responseBody.get("phoneNumber")).isEqualTo(customer.getPhoneNumber());
        assertThat(responseBody.get("address")).isEqualTo(customer.getAddress());
        assertThat(responseBody.get("grade")).isEqualTo(customer.getGrade().toString());
        assertThat(responseBody.get("orderCount")).isEqualTo(customer.getOrderCount());
    }


    @Test
    @org.junit.jupiter.api.Order(3)
    void 메뉴_목록_조회_API_BASIC_고객_테스트() {
        // 메뉴 목록 조회 API 호출 (BASIC 고객)
        ResponseEntity<Map<String, Object>> response = menuController.getAllMenus(customer.getId());
        
        // 응답 검증
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        Map<String, Object> responseBody = response.getBody();
        assertThat(responseBody).isNotNull();
        
        // 메뉴 목록 검증
        @SuppressWarnings("unchecked")
        java.util.List<Map<String, Object>> menus = (java.util.List<Map<String, Object>>) responseBody.get("menus");
        assertThat(menus).isNotEmpty();
        
        // BASIC 고객은 할인가 없음
        Map<String, Object> menuData = menus.get(0);
        assertThat(menuData.get("menuId")).isEqualTo(menu.getId());
        assertThat(menuData.get("name")).isEqualTo(menu.getName());
        assertThat(menuData.get("price")).isEqualTo(menu.getPrice());
        assertThat(menuData.get("discountedPrice")).isNull(); // BASIC 고객은 할인가 없음
    }
}
