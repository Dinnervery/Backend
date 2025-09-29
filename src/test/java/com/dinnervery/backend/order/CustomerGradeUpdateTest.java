package com.dinnervery.backend.order;

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
import com.dinnervery.backend.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CustomerGradeUpdateTest {

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

    @BeforeEach
    void setUp() {
        // 고객 생성
        customer = Customer.builder()
                .loginId("test_customer")
                .password("password")
                .name("테스트 고객")
                .phoneNumber("010-1234-5678")
                .build();
        customer = customerRepository.save(customer);

        // 주소 생성
        address = Address.builder()
                .customer(customer)
                .addrDetail("서울시 강남구 테헤란로 123")
                .build();
        address = addressRepository.save(address);

        // 메뉴 생성
        menu = Menu.builder()
                .name("테스트 메뉴")
                .price(new BigDecimal("5000"))
                .description("테스트용 메뉴")
                .build();
        menu = menuRepository.save(menu);

        // 서빙 스타일 생성
        servingStyle = ServingStyle.builder()
                .name("일반")
                .extraPrice(new BigDecimal("0"))
                .build();
        servingStyle = servingStyleRepository.save(servingStyle);
    }

    @Test
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
                .orderedQty(1)
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

        // 10번째 주문까지 완료하여 VIP 등급 달성
        for (int i = 2; i <= 10; i++) {
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
                    .orderedQty(1)
                    .build();
            newOrder.addOrderItem(newOrderItem);
            newOrder = orderRepository.save(newOrder);

            orderService.completeOrder(newOrder.getId());
        }

        // 최종 고객 정보 확인
        Customer finalCustomer = customerRepository.findById(customer.getId()).orElseThrow();
        assertThat(finalCustomer.getOrderCount()).isEqualTo(10);
        assertThat(finalCustomer.getGrade()).isEqualTo(Customer.CustomerGrade.VIP);
    }

    @Test
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

        // 주문수 10으로 설정하여 VIP 등급 달성
        for (int i = 0; i < 5; i++) {
            customer.incrementOrderCount();
        }
        customer = customerRepository.save(customer);
        assertThat(customer.getOrderCount()).isEqualTo(10);
        assertThat(customer.getGrade()).isEqualTo(Customer.CustomerGrade.VIP);
    }

    @Test
    void VIP_할인_적용_조건_테스트() {
        // 10번 주문하여 VIP 등급 달성
        for (int i = 1; i <= 10; i++) {
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
                    .orderedQty(1)
                    .build();
            order.addOrderItem(orderItem);
            order = orderRepository.save(order);

            orderService.completeOrder(order.getId());
        }

        Customer vipCustomer = customerRepository.findById(customer.getId()).orElseThrow();
        assertThat(vipCustomer.getGrade()).isEqualTo(Customer.CustomerGrade.VIP);
        assertThat(vipCustomer.getOrderCount()).isEqualTo(10);

        // 11번째 주문 시 VIP 할인 적용 가능
        assertThat(vipCustomer.isVipDiscountEligible()).isTrue();

        // 12번째 주문 완료 후
        Order order11 = Order.builder()
                .customer(customer)
                .address(address)
                .cardNumber("1234-5678-9012-3456")
                .build();
        order11 = orderRepository.save(order11);

        // 주문 항목 추가
        OrderItem orderItem11 = OrderItem.builder()
                .menu(menu)
                .servingStyle(servingStyle)
                .orderedQty(1)
                .build();
        order11.addOrderItem(orderItem11);
        order11 = orderRepository.save(order11);

        orderService.completeOrder(order11.getId());

        Customer updatedCustomer = customerRepository.findById(customer.getId()).orElseThrow();
        assertThat(updatedCustomer.getOrderCount()).isEqualTo(11);
        assertThat(updatedCustomer.isVipDiscountEligible()).isFalse();
    }
}
