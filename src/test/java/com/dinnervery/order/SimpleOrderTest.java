package com.dinnervery.order;

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
import com.dinnervery.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class SimpleOrderTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private MenuRepository menuRepository;

    @Autowired
    private ServingStyleRepository servingStyleRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Customer customer;
    private Address address;
    private Menu menu;
    private ServingStyle servingStyle;

    @BeforeEach
    void setUp() {
        // SQL을 사용하여 강제 초기화 (외래키 제약조건 무시)
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY FALSE");
        jdbcTemplate.execute("TRUNCATE TABLE order_item_options");
        jdbcTemplate.execute("TRUNCATE TABLE order_items");
        jdbcTemplate.execute("TRUNCATE TABLE orders");
        jdbcTemplate.execute("TRUNCATE TABLE addresses");
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
                .name("기본 스타일")
                .extraPrice(0)
                .build();
        servingStyle = servingStyleRepository.save(servingStyle);
    }

    @Test
    @Transactional
    void testSimpleOrderCompletion() {
        // 주문 생성
        Order order = Order.builder()
                .customer(customer)
                .address(address)
                .cardNumber("1234-5678-9012-3456")
                .deliveryTime(LocalTime.of(20, 0))
                .build();
        order = orderRepository.save(order);

        // 주문 아이템 추가
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
    }
}
