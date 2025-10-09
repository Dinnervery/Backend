package com.dinnervery.order;

import com.dinnervery.controller.OrderController;
import com.dinnervery.entity.Address;
import com.dinnervery.entity.Customer;
import com.dinnervery.entity.Employee;
import com.dinnervery.entity.Order;
import com.dinnervery.repository.AddressRepository;
import com.dinnervery.repository.CustomerRepository;
import com.dinnervery.repository.EmployeeRepository;
import com.dinnervery.repository.OrderRepository;

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

import java.util.Map;
import java.util.UUID;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Transactional
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
    private AddressRepository addressRepository;

    @BeforeEach
    void setUp() {
        // given - 테스트 데이터 생성
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
                .deliveryTime(LocalTime.of(20, 0))
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
    void testOrderStatusTransition() {
        // given - 초기 상태 확인
        assertThat(order.getDeliveryStatus()).isEqualTo(Order.Status.REQUESTED);
        assertThat(order.getRequestedAt()).isNotNull();

        // when - 조리 시작
        order.startCooking();
        order = orderRepository.save(order);
        
        // then - 상태 검증
        assertThat(order.getDeliveryStatus()).isEqualTo(Order.Status.COOKING);
        assertThat(order.getCookingAt()).isNotNull();

        // when - 조리 완료
        order.completeCooking();
        order = orderRepository.save(order);
        
        // then - 상태 검증
        assertThat(order.getDeliveryStatus()).isEqualTo(Order.Status.COOKED);
        
        // when - 배달 시작
        order.startDelivering();
        order = orderRepository.save(order);
        
        // then - 상태 검증
        assertThat(order.getDeliveryStatus()).isEqualTo(Order.Status.DELIVERING);
        assertThat(order.getDeliveringAt()).isNotNull();

        // when - 배달 완료
        order.completeDelivery();
        order = orderRepository.save(order);
        
        // then - 상태 검증
        assertThat(order.getDeliveryStatus()).isEqualTo(Order.Status.DONE);
        assertThat(order.getDoneAt()).isNotNull();
    }

    @Test
    void testInvalidStatusTransitionException() {
        // given & when & then - REQUESTED 상태에서 배달 시작 시도
        assertThatThrownBy(() -> order.startDelivering())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("배달 시작은 COOKED 상태에서만 가능합니다");

        // given & when & then - REQUESTED 상태에서 배달 완료 시도
        assertThatThrownBy(() -> order.completeDelivery())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("배달 완료는 DELIVERING 상태에서만 가능합니다");

        // given - 조리 시작 후 잘못된 상태 변경 시도
        order.startCooking();
        order = orderRepository.save(order);

        // when & then - COOKING 상태에서 배달 완료 시도
        assertThatThrownBy(() -> order.completeDelivery())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("배달 완료는 DELIVERING 상태에서만 가능합니다");
    }


    @Test
    void testEmployeePermissions() {
        // given & when & then - 조리사 권한 확인
        assertThat(cook.hasCookPermission()).isTrue();
        assertThat(cook.hasDeliveryPermission()).isFalse();

        // given & when & then - 배달원 권한 확인
        assertThat(deliver.hasDeliveryPermission()).isTrue();
        assertThat(deliver.hasCookPermission()).isFalse();
    }

    @Test
    void testOrderStatusUpdateAPI() {
        // given - 주문 상태를 COOKING으로 업데이트
        Map<String, Object> statusUpdate = Map.of("status", "COOKING");
        
        // when - 주문 상태 업데이트 API 호출
        ResponseEntity<com.dinnervery.dto.response.OrderUpdateResponse> response = orderController.updateOrderStatus(order.getId(), statusUpdate);
        
        // then - 응답 검증
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        com.dinnervery.dto.response.OrderUpdateResponse responseBody = response.getBody();
        assertThat(responseBody).isNotNull();
        
        // 상태 업데이트 검증
        if (responseBody != null) {
            assertThat(responseBody.getOrderId()).isEqualTo(order.getId());
            assertThat(responseBody.getStatus()).isEqualTo("COOKING");
        }
        
        // 실제 주문 상태 확인
        Order updatedOrder = orderRepository.findById(order.getId()).orElseThrow();
        assertThat(updatedOrder.getDeliveryStatus()).isEqualTo(Order.Status.COOKING);
    }

    @Test
    void testCookingOrderListRetrievalAPI() {
        // given - 주문을 COOKING 상태로 변경
        order.startCooking();
        order = orderRepository.save(order);
        
        // when - 조리 대기목록 조회 API 호출
        ResponseEntity<com.dinnervery.dto.response.OrderListResponse> response = orderController.getCookingOrders();
        
        // then - 응답 검증
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        com.dinnervery.dto.response.OrderListResponse responseBody = response.getBody();
        assertThat(responseBody).isNotNull();
        
        // 주문 목록 검증
        if (responseBody != null) {
            assertThat(responseBody.getOrders()).isNotEmpty();
            
            // 첫 번째 주문 검증
            com.dinnervery.dto.response.OrderListResponse.OrderSummary orderData = responseBody.getOrders().get(0);
            assertThat(orderData.getOrderId()).isEqualTo(order.getId());
            assertThat(orderData.getStatus()).isEqualTo("COOKING");
            assertThat(orderData.getDeliveryTime()).isNotNull();
            assertThat(orderData.getOrderedItems()).isNotNull();
        }
    }

    @Test
    void testDeliveryOrderListRetrievalAPI() {
        // given - 주문을 COOKED 상태로 변경
        order.startCooking();
        order.completeCooking();
        order = orderRepository.save(order);
        
        // when - 배달 대기목록 조회 API 호출
        ResponseEntity<com.dinnervery.dto.response.OrderListResponse> response = orderController.getDeliveryOrders();
        
        // then - 응답 검증
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        com.dinnervery.dto.response.OrderListResponse responseBody = response.getBody();
        assertThat(responseBody).isNotNull();
        
        // 주문 목록 검증
        if (responseBody != null) {
            assertThat(responseBody.getOrders()).isNotEmpty();
            
            // 첫 번째 주문 검증
            com.dinnervery.dto.response.OrderListResponse.OrderSummary orderData = responseBody.getOrders().get(0);
            assertThat(orderData.getOrderId()).isEqualTo(order.getId());
            assertThat(orderData.getStatus()).isEqualTo("COOKED");
            assertThat(orderData.getDeliveryTime()).isNotNull();
            assertThat(orderData.getOrderedItems()).isNotNull();
        }
    }
}
