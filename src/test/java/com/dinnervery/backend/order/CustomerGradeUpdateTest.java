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
import com.dinnervery.backend.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
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

    @BeforeEach
    void setUp() {
        // 고객 생성
        customer = Customer.builder()
                .loginId("test_customer_" + System.currentTimeMillis())
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
                .name("일반")
                .extraPrice(0)
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
        // 15번 주문하여 VIP 등급 달성
        for (int i = 1; i <= 15; i++) {
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

            orderService.completeOrder(order.getId());
        }

        Customer vipCustomer = customerRepository.findById(customer.getId()).orElseThrow();
        assertThat(vipCustomer.getGrade()).isEqualTo(Customer.CustomerGrade.VIP);
        assertThat(vipCustomer.getOrderCount()).isEqualTo(15);

        // 16번째 주문 시 VIP 할인 적용 가능
        assertThat(vipCustomer.isVipDiscountEligible()).isTrue();

        // 17번째 주문 완료 후
        Order order16 = Order.builder()
                .customer(customer)
                .address(address)
                .cardNumber("1234-5678-9012-3456")
                .build();
        order16 = orderRepository.save(order16);

        // 주문 항목 추가
        OrderItem orderItem16 = OrderItem.builder()
                .menu(menu)
                .servingStyle(servingStyle)
                .quantity(1)
                .build();
        order16.addOrderItem(orderItem16);
        order16 = orderRepository.save(order16);

        orderService.completeOrder(order16.getId());

        Customer updatedCustomer = customerRepository.findById(customer.getId()).orElseThrow();
        assertThat(updatedCustomer.getOrderCount()).isEqualTo(16);
        assertThat(updatedCustomer.isVipDiscountEligible()).isFalse();
    }

    @Test
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
    void 메뉴_목록_조회_API_VIP_할인_테스트() {
        // 고객을 VIP로 만들기 위해 주문수 10회로 설정
        for (int i = 0; i < 10; i++) {
            customer.incrementOrderCount();
        }
        customer = customerRepository.save(customer);
        
        // 메뉴 목록 조회 API 호출 (VIP 고객)
        ResponseEntity<Map<String, Object>> response = menuController.getAllMenus(customer.getId());
        
        // 응답 검증
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        Map<String, Object> responseBody = response.getBody();
        assertThat(responseBody).isNotNull();
        
        // 메뉴 목록 검증
        @SuppressWarnings("unchecked")
        java.util.List<Map<String, Object>> menus = (java.util.List<Map<String, Object>>) responseBody.get("menus");
        assertThat(menus).isNotEmpty();
        
        // VIP 할인가 검증
        Map<String, Object> menuData = menus.get(0);
        assertThat(menuData.get("menuId")).isEqualTo(menu.getId());
        assertThat(menuData.get("name")).isEqualTo(menu.getName());
        assertThat(menuData.get("price")).isEqualTo(menu.getPrice());
        assertThat(menuData.get("discountedPrice")).isNotNull();
        
        // 할인가 계산 검증 (10% 할인)
        int expectedDiscountedPrice = (int) (menu.getPrice() * 0.9);
        assertThat(menuData.get("discountedPrice")).isEqualTo(expectedDiscountedPrice);
    }

    @Test
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
