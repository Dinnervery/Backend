package com.dinnervery.backend.order;

import com.dinnervery.backend.controller.OrderController;
import com.dinnervery.backend.entity.Address;
import com.dinnervery.backend.entity.Customer;
import com.dinnervery.backend.entity.Employee;
import com.dinnervery.backend.entity.Order;
import com.dinnervery.backend.repository.AddressRepository;
import com.dinnervery.backend.repository.CustomerRepository;
import com.dinnervery.backend.repository.EmployeeRepository;
import com.dinnervery.backend.repository.OrderRepository;

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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class OrderStatusTransitionTest {

    @Autowired
    private OrderController orderController;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    private Customer customer;
    private Order order;
    private Employee cook;
    private Employee deliver;
    private Address address1;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private AddressRepository addressRepository;

    @BeforeEach
    void setUp() {
        // SQL을 사용한 강제 테이블 정리 (외래키 제약조건 무시)
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY FALSE");
        jdbcTemplate.execute("TRUNCATE TABLE addresses");
        jdbcTemplate.execute("TRUNCATE TABLE orders");
        jdbcTemplate.execute("TRUNCATE TABLE employees");
        jdbcTemplate.execute("TRUNCATE TABLE customers");
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
        address1 = Address.builder()
                .customer(customer)
                .address("서울시 강남구 테헤란로 123")
                .build();
        address1 = addressRepository.save(address1);

        // 주문 생성
        order = Order.builder()
                .customer(customer)
                .address(address1)
                .cardNumber("1234-5678-9012-3456")
                .build();
        order = orderRepository.save(order);

        // 직원 생성
        cook = Employee.builder()
                .loginId("cook1_" + UUID.randomUUID())
                .password("password")
                .name("요리사")
                .phoneNumber("010-1111-1111")
                .task(Employee.EmployeeTask.COOK)
                .build();
        cook = employeeRepository.save(cook);

        deliver = Employee.builder()
                .loginId("deliver1_" + UUID.randomUUID())
                .password("password")
                .name("배달원")
                .phoneNumber("010-2222-2222")
                .task(Employee.EmployeeTask.DELIVERY)
                .build();
        deliver = employeeRepository.save(deliver);
    }

    @Test
    void 주문_상태_전이_테스트() {
        // 초기 상태 확인
        assertThat(order.getDeliveryStatus()).isEqualTo(Order.status.REQUESTED);
        assertThat(order.getRequestedAt()).isNotNull();

        // 조리 시작
        order.startCooking();
        order = orderRepository.save(order);
        assertThat(order.getDeliveryStatus()).isEqualTo(Order.status.COOKING);
        assertThat(order.getCookingAt()).isNotNull();

        // 조리 완료 (자동으로 배달 대기 상태로 변경)
        order.completeCooking();
        order = orderRepository.save(order);
        assertThat(order.getDeliveryStatus()).isEqualTo(Order.status.DELIVERING);
        assertThat(order.getDeliveringAt()).isNotNull();

        // 배달 완료
        order.completeDelivery();
        order = orderRepository.save(order);
        assertThat(order.getDeliveryStatus()).isEqualTo(Order.status.DONE);
        assertThat(order.getDoneAt()).isNotNull();
    }

    @Test
    void 잘못된_상태_전이_예외_테스트() {
        // REQUESTED 상태에서 배달 시작 시도
        assertThatThrownBy(() -> order.startDelivering())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("배달 시작은 DELIVERING 상태에서만 가능합니다");

        // REQUESTED 상태에서 배달 완료 시도
        assertThatThrownBy(() -> order.completeDelivery())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("배달 완료는 DELIVERING 상태에서만 가능합니다");

        // 조리 시작 후 잘못된 전이 시도
        order.startCooking();
        order = orderRepository.save(order);

        // COOKING 상태에서 배달 완료 시도
        assertThatThrownBy(() -> order.completeDelivery())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("배달 완료는 DELIVERING 상태에서만 가능합니다");
    }

    @Test
    void 주문_취소_테스트() {
        // REQUESTED 상태에서 취소
        order.cancelOrder();
        order = orderRepository.save(order);
        assertThat(order.getDeliveryStatus()).isEqualTo(Order.status.CANCELED);
        assertThat(order.getCanceledAt()).isNotNull();

        // COOKING 상태에서 취소
        Order order2 = Order.builder()
                .customer(customer)
                .address(address1)
                .cardNumber("1234-5678-9012-3456")
                .build();
        order2 = orderRepository.save(order2);
        order2.startCooking();
        order2.cancelOrder();
        order2 = orderRepository.save(order2);
        assertThat(order2.getDeliveryStatus()).isEqualTo(Order.status.CANCELED);
        assertThat(order2.getCanceledAt()).isNotNull();

        // DONE 상태에서 취소 시도
        Order order3 = Order.builder()
                .customer(customer)
                .address(address1)
                .cardNumber("1234-5678-9012-3456")
                .build();
        order3 = orderRepository.save(order3);
        order3.startCooking();
        order3.completeCooking();
        order3.completeDelivery();
        final Order finalOrder3 = orderRepository.save(order3);

        assertThatThrownBy(() -> finalOrder3.cancelOrder())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("완료된 주문은 취소할 수 없습니다");
    }

    @Test
    void 직원_권한_테스트() {
        // 요리사 권한 확인
        assertThat(cook.hasCookPermission()).isTrue();
        assertThat(cook.hasDeliveryPermission()).isFalse();

        // 배달원 권한 확인
        assertThat(deliver.hasDeliveryPermission()).isTrue();
        assertThat(deliver.hasCookPermission()).isFalse();
    }

    @Test
    void 주문_상태_업데이트_API_테스트() {
        // 주문 상태를 COOKING으로 업데이트
        Map<String, Object> statusUpdate = Map.of("status", "COOKING");
        ResponseEntity<Map<String, Object>> response = orderController.updateOrderStatus(order.getId(), statusUpdate);
        
        // 응답 검증
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        Map<String, Object> responseBody = response.getBody();
        assertThat(responseBody).isNotNull();
        
        // 상태 업데이트 검증
        assertThat(responseBody).isNotNull();
        assertThat(responseBody.get("orderId")).isEqualTo(order.getId());
        assertThat(responseBody.get("status")).isEqualTo("COOKING");
        
        // 실제 주문 상태 확인
        Order updatedOrder = orderRepository.findById(order.getId()).orElseThrow();
        assertThat(updatedOrder.getDeliveryStatus()).isEqualTo(Order.status.COOKING);
    }

    @Test
    void 조리_대기_목록_조회_API_테스트() {
        // 주문을 COOKING 상태로 변경
        order.startCooking();
        order = orderRepository.save(order);
        
        // 조리 대기 목록 조회 API 호출
        ResponseEntity<Map<String, Object>> response = orderController.getCookingOrders();
        
        // 응답 검증
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        Map<String, Object> responseBody = response.getBody();
        assertThat(responseBody).isNotNull();
        
        // 주문 목록 검증
        @SuppressWarnings("unchecked")
        java.util.List<Map<String, Object>> orders = (java.util.List<Map<String, Object>>) responseBody.get("orders");
        assertThat(orders).isNotEmpty();
        
        // 첫 번째 주문 검증
        Map<String, Object> orderData = orders.get(0);
        assertThat(orderData.get("orderId")).isEqualTo(order.getId());
        assertThat(orderData.get("status")).isEqualTo("COOKING");
        assertThat(orderData.get("deliveryTime")).isNotNull();
        assertThat(orderData.get("orderedItems")).isNotNull();
    }

    @Test
    void 배달_대기_목록_조회_API_테스트() {
        // 주문을 COOKED 상태로 변경
        order.startCooking();
        order.completeCooking();
        order = orderRepository.save(order);
        
        // 배달 대기 목록 조회 API 호출
        ResponseEntity<Map<String, Object>> response = orderController.getDeliveryOrders();
        
        // 응답 검증
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        Map<String, Object> responseBody = response.getBody();
        assertThat(responseBody).isNotNull();
        
        // 주문 목록 검증
        @SuppressWarnings("unchecked")
        java.util.List<Map<String, Object>> orders = (java.util.List<Map<String, Object>>) responseBody.get("orders");
        assertThat(orders).isNotEmpty();
        
        // 첫 번째 주문 검증
        Map<String, Object> orderData = orders.get(0);
        assertThat(orderData.get("orderId")).isEqualTo(order.getId());
        assertThat(orderData.get("status")).isEqualTo("COOKED");
        assertThat(orderData.get("deliveryTime")).isNotNull();
        assertThat(orderData.get("orderedItems")).isNotNull();
    }
}
