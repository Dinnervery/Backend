package com.dinnervery.backend.order;

import com.dinnervery.backend.dto.OrderDto;
import com.dinnervery.backend.dto.OrderItemDto;
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
import com.dinnervery.backend.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ReorderTest {

    @Autowired
    private OrderService orderService;

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

    private Customer customer;
    private Menu menu1;
    private Menu menu2;
    private Address address1;
    private Address address2;
    private ServingStyle servingStyle;
    private Order originalOrder;

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

        // 메뉴 생성
        menu1 = Menu.builder()
                .name("김치찌개")
                .price(new BigDecimal("8000"))
                .description("맛있는 김치찌개")
                .build();
        menu1 = menuRepository.save(menu1);

        menu2 = Menu.builder()
                .name("된장찌개")
                .price(new BigDecimal("7000"))
                .description("맛있는 된장찌개")
                .build();
        menu2 = menuRepository.save(menu2);

        // 서빙 스타일 생성
        servingStyle = ServingStyle.builder()
                .name("일반")
                .extraPrice(new BigDecimal("0"))
                .build();
        servingStyle = servingStyleRepository.save(servingStyle);

        // 주소 생성
        address1 = Address.builder()
                .customer(customer)
                .addrDetail("서울시 강남구 테헤란로 123, 101동 1001호")
                .build();
        address1 = addressRepository.save(address1);

        address2 = Address.builder()
                .customer(customer)
                .addrDetail("서울시 서초구 서초대로 456, 202동 2002호")
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
                .orderedQty(2)
                .build();
        originalOrder.addOrderItem(orderItem1);

        OrderItem orderItem2 = OrderItem.builder()
                .menu(menu2)
                .servingStyle(servingStyle)
                .orderedQty(1)
                .build();
        originalOrder.addOrderItem(orderItem2);

        originalOrder = orderRepository.save(originalOrder);
    }

    @Test
    void 재주문_테스트() {
        // 재주문 요청
        ReorderRequest reorderRequest = ReorderRequest.builder()
                .customerId(customer.getId())
                .addressId(address2.getId()) // 다른 주소로 재주문
                .cardNumber("9876-5432-1098-7654") // 다른 카드로 재주문
                .build();

        // 재주문 실행
        OrderDto reorderedOrder = orderService.reorder(originalOrder.getId(), reorderRequest);

        // 재주문 결과 검증
        assertThat(reorderedOrder.getId()).isNotEqualTo(originalOrder.getId());
        assertThat(reorderedOrder.getCustomerId()).isEqualTo(customer.getId());
        assertThat(reorderedOrder.getOrderItems()).hasSize(2);

        // 주문 항목 검증
        List<OrderItemDto> reorderedItems = reorderedOrder.getOrderItems();
        assertThat(reorderedItems).hasSize(2);

        // 첫 번째 주문 항목 검증 (김치찌개)
        OrderItemDto reorderedItem1 = reorderedItems.stream()
                .filter(item -> item.getMenuId().equals(menu1.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(reorderedItem1.getOrderedQty()).isEqualTo(2);

        // 두 번째 주문 항목 검증 (된장찌개)
        OrderItemDto reorderedItem2 = reorderedItems.stream()
                .filter(item -> item.getMenuId().equals(menu2.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(reorderedItem2.getOrderedQty()).isEqualTo(1);

        // 원본 주문은 변경되지 않았는지 확인
        Order savedOriginalOrder = orderRepository.findByIdWithDetails(originalOrder.getId()).orElseThrow();
        assertThat(savedOriginalOrder.getOrderItems()).hasSize(2);
    }

    @Test
    void 존재하지_않는_주문_재주문_예외_테스트() {
        ReorderRequest reorderRequest = ReorderRequest.builder()
                .customerId(customer.getId())
                .addressId(address2.getId())
                .cardNumber("9876-5432-1098-7654")
                .build();

        // 존재하지 않는 주문 ID로 재주문 시도
        assertThatThrownBy(() -> orderService.reorder(999L, reorderRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("원본 주문을 찾을 수 없습니다");
    }

    @Test
    void 존재하지_않는_고객_재주문_예외_테스트() {
        ReorderRequest reorderRequest = ReorderRequest.builder()
                .customerId(999L) // 존재하지 않는 고객 ID
                .addressId(address2.getId())
                .cardNumber("9876-5432-1098-7654")
                .build();

        assertThatThrownBy(() -> orderService.reorder(originalOrder.getId(), reorderRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("고객을 찾을 수 없습니다");
    }

    @Test
    void 존재하지_않는_주소_재주문_예외_테스트() {
        ReorderRequest reorderRequest = ReorderRequest.builder()
                .customerId(customer.getId())
                .addressId(999L) // 존재하지 않는 주소 ID
                .cardNumber("9876-5432-1098-7654")
                .build();

        assertThatThrownBy(() -> orderService.reorder(originalOrder.getId(), reorderRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("주소를 찾을 수 없습니다");
    }
}
