package com.dinnervery.order;

import com.dinnervery.entity.Customer;
import com.dinnervery.entity.Menu;
import com.dinnervery.entity.Order;
import com.dinnervery.entity.OrderItem;
import com.dinnervery.entity.Style;
import com.dinnervery.repository.CustomerRepository;
import com.dinnervery.repository.MenuRepository;
import com.dinnervery.repository.OrderRepository;
import com.dinnervery.repository.StyleRepository;
import com.dinnervery.service.OrderService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CustomerGradeIntegrationTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private MenuRepository menuRepository;

    @Autowired
    private StyleRepository styleRepository;

    private Customer customer;
    private Menu menu;
    private Style style;

    @BeforeEach
    void setUp() {
        customer = Customer.builder()
                .loginId("test_customer_" + UUID.randomUUID())
                .password("password")
                .name("테스트 고객")
                .phoneNumber("010-0000-0000")
                .address("테스트 주소")
                .build();
        customer = customerRepository.save(customer);

        menu = Menu.builder()
                .name("테스트 메뉴")
                .price(10000)
                .build();
        menu = menuRepository.save(menu);

        style = Style.builder()
                .name("기본")
                .extraPrice(0)
                .build();
        style = styleRepository.save(style);
    }

    @Test
    void testOrderCompletionAndCustomerGradeUpdate() {
        assertThat(customer.getOrderCount()).isEqualTo(0);
        assertThat(customer.getGrade()).isEqualTo(Customer.CustomerGrade.BASIC);

        for (int i = 0; i < 15; i++) {
            Order order = Order.builder()
                    .customer(customer)
                    .address("테스트 주소")
                    .cardNumber("1234")
                    .deliveryTime(LocalTime.of(20, 0))
                    .build();

            OrderItem orderItem = OrderItem.builder()
                    .menu(menu)
                    .style(style)
                    .quantity(1)
                    .build();
            order.addOrderItem(orderItem);

            Order savedOrder = orderRepository.save(order);
            orderService.completeOrder(savedOrder.getId());
        }

        Customer finalCustomer = customerRepository.findById(customer.getId()).orElseThrow();
        assertThat(finalCustomer.getOrderCount()).isEqualTo(15);
        assertThat(finalCustomer.getGrade()).isEqualTo(Customer.CustomerGrade.VIP);
    }
}


