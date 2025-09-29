package com.dinnervery.backend.order;

import com.dinnervery.backend.dto.order.OrderItemOptionRequest;
import com.dinnervery.backend.dto.order.OrderItemRequest;
import com.dinnervery.backend.entity.Customer;
import com.dinnervery.backend.repository.CustomerRepository;
import com.dinnervery.backend.repository.MenuOptionRepository;
import com.dinnervery.backend.entity.Menu;
import com.dinnervery.backend.entity.MenuOption;
import com.dinnervery.backend.repository.MenuRepository;
import com.dinnervery.backend.repository.OrderItemOptionRepository;
import com.dinnervery.backend.repository.OrderRepository;
import com.dinnervery.backend.repository.ServingStyleRepository;
import com.dinnervery.backend.repository.OrderItemRepository;
import com.dinnervery.backend.entity.Order;
import com.dinnervery.backend.entity.OrderItem;
import com.dinnervery.backend.entity.OrderItemOption;
import com.dinnervery.backend.entity.ServingStyle;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class OrderIntegrationTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private OrderItemOptionRepository orderItemOptionRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private MenuRepository menuRepository;


    @Autowired
    private MenuOptionRepository menuOptionRepository;

    @Autowired
    private ServingStyleRepository servingStyleRepository;

    private Customer testCustomer;
    private Menu testMenu;
    private Menu testMenuEntity;
    private MenuOption testMenuOption;
    private ServingStyle testServingStyle;

    @BeforeEach
    void setUp() {
        // 테스트 고객 생성
        testCustomer = Customer.builder()
                .loginId("test123")
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
                .name("테스트 메뉴")
                .price(new BigDecimal("50000"))
                .description("통합 테스트용 메뉴")
                .build();
        testMenu = menuRepository.save(testMenu);

        // MenuOption을 위한 별도의 Menu 생성
        testMenuEntity = Menu.builder()
                .name("테스트 메뉴 엔티티")
                .price(new BigDecimal("50000"))
                .description("통합 테스트용 메뉴 엔티티")
                .build();
        testMenuEntity = menuRepository.save(testMenuEntity);

        // 테스트 메뉴 옵션 생성
        testMenuOption = MenuOption.builder()
                .menu(testMenuEntity)
                .itemName("스테이크")
                .itemPrice(10000)
                .build();
        testMenuOption = menuOptionRepository.save(testMenuOption);

        // 테스트 서빙 스타일 생성
        testServingStyle = ServingStyle.builder()
                .name("TEST_STYLE")
                .extraPrice(new BigDecimal("5000"))
                .build();
        testServingStyle = servingStyleRepository.save(testServingStyle);
    }

    @Test
    void 주문_생성_후_저장된_데이터_검증() {
        // Given
        OrderItemOptionRequest optionRequest = OrderItemOptionRequest.builder()
                .menuOptionId(testMenuOption.getId())
                .orderedQty(2) // 기본 1개에서 2개로 설정
                .build();

        OrderItemRequest itemRequest = OrderItemRequest.builder()
                .menuId(testMenu.getId())
                .servingStyleId(testServingStyle.getId())
                .orderedQty(1)
                .options(List.of(optionRequest))
                .build();

        // 주문 생성
        Order order = Order.builder()
                .customer(testCustomer)
                .build();
        orderRepository.save(order);

        // 주문 항목 생성
        OrderItem orderItem = OrderItem.builder()
                .menu(testMenu)
                .orderedQty(itemRequest.getOrderedQty())
                .build();
        order.addOrderItem(orderItem);
        OrderItem savedOrderItem = orderItemRepository.save(orderItem);

        // 주문 항목 옵션 생성
        OrderItemOption orderItemOption = OrderItemOption.builder()
                .menuOption(testMenuOption)
                .orderedQty(optionRequest.getOrderedQty())
                .build();
        orderItem.addOrderItemOption(orderItemOption);
        OrderItemOption savedOrderItemOption = orderItemOptionRepository.save(orderItemOption);

        // 고객 주문 횟수 증가
        testCustomer.incrementOrderCount();
        Customer updatedCustomer = customerRepository.save(testCustomer);

        // Then
        // 저장된 주문 검증
        assertThat(order.getId()).isNotNull();
        assertThat(order.getCustomer().getId()).isEqualTo(testCustomer.getId());
        assertThat(order.getTotalAmount()).isNotNull();
        assertThat(order.getFinalAmount()).isNotNull();

        // 저장된 주문 항목 검증
        assertThat(savedOrderItem.getId()).isNotNull();
        assertThat(savedOrderItem.getOrder().getId()).isEqualTo(order.getId());
        assertThat(savedOrderItem.getMenu().getId()).isEqualTo(testMenu.getId());
        assertThat(savedOrderItem.getOrderedQty()).isEqualTo(1);

        // 저장된 주문 항목 옵션 검증
        assertThat(savedOrderItemOption.getId()).isNotNull();
        assertThat(savedOrderItemOption.getOrderItem().getId()).isEqualTo(savedOrderItem.getId());
        assertThat(savedOrderItemOption.getMenuOption().getId()).isEqualTo(testMenuOption.getId());
        assertThat(savedOrderItemOption.getOrderedQty()).isEqualTo(2);

        // 고객 주문 횟수 증가 검증
        assertThat(updatedCustomer.getOrderCount()).isEqualTo(6); // 5 -> 6으로 증가

        // 고객 등급 변경 검증 (6번째 주문이므로 아직 BASIC)
        assertThat(updatedCustomer.getGrade()).isEqualTo(Customer.CustomerGrade.BASIC);
    }

    @Test
    void 고객_주문횟수_10회_달성시_VIP_등급_변경() {
        // Given
        // 고객의 주문 횟수를 9회로 설정
        testCustomer = customerRepository.findById(testCustomer.getId()).orElseThrow();
        for (int i = 0; i < 9; i++) {
            testCustomer.incrementOrderCount();
        }
        testCustomer = customerRepository.save(testCustomer);

        OrderItemRequest itemRequest = OrderItemRequest.builder()
                .menuId(testMenu.getId())
                .servingStyleId(testServingStyle.getId())
                .orderedQty(1)
                .options(List.of())
                .build();

        // When
        // 주문 생성
        Order order = Order.builder()
                .customer(testCustomer)
                .build();
        orderRepository.save(order);

        // 주문 항목 생성
        OrderItem orderItem = OrderItem.builder()
                .menu(testMenu)
                .orderedQty(itemRequest.getOrderedQty())
                .build();
        order.addOrderItem(orderItem);
        orderItemRepository.save(orderItem);

        // 고객 주문 횟수 증가 (10번째 주문)
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
        // Given
        Order order = Order.builder()
                .customer(testCustomer)
                .build();
        orderRepository.save(order);

        OrderItem orderItem = OrderItem.builder()
                .menu(testMenu)
                .orderedQty(1)
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
