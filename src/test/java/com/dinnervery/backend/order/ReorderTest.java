package com.dinnervery.backend.order;

import com.dinnervery.backend.controller.OrderController;
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
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
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
                .detailAddress("101동 1001호")
                .build();
        address1 = addressRepository.save(address1);

        address2 = Address.builder()
                .customer(customer)
                .address("서울시 서초구 서초대로 456")
                .detailAddress("202동 2002호")
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
    void 재주문_API_테스트() {
        // 재주문 API 호출
        ResponseEntity<Map<String, Object>> response = orderController.reorder(originalOrder.getId());
        
        // 응답 검증
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        Map<String, Object> responseBody = response.getBody();
        assertThat(responseBody).isNotNull();
        
        // orderedItems 검증
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> orderedItems = (List<Map<String, Object>>) responseBody.get("orderedItems");
        assertThat(orderedItems).hasSize(2);
        
        // 첫 번째 주문 항목 검증 (발렌타인 디너)
        Map<String, Object> firstItem = orderedItems.stream()
                .filter(item -> item.get("menuId").equals(menu1.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(firstItem.get("menuId")).isEqualTo(menu1.getId());
        assertThat(firstItem.get("quantity")).isEqualTo(2);
        assertThat(firstItem.get("servingStyleId")).isEqualTo(servingStyle.getId());
        
        // 두 번째 주문 항목 검증 (잉글리시 디너)
        Map<String, Object> secondItem = orderedItems.stream()
                .filter(item -> item.get("menuId").equals(menu2.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(secondItem.get("menuId")).isEqualTo(menu2.getId());
        assertThat(secondItem.get("quantity")).isEqualTo(1);
        assertThat(secondItem.get("servingStyleId")).isEqualTo(servingStyle.getId());
    }

    @Test
    void 존재하지_않는_주문_재주문_예외_테스트() {
        // 존재하지 않는 주문 ID로 재주문 시도
        assertThatThrownBy(() -> orderController.reorder(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("원본 주문을 찾을 수 없습니다");
    }
}
