package com.dinnervery.backend.order;

import com.dinnervery.backend.controller.CartController;
import com.dinnervery.backend.controller.MemberController;
import com.dinnervery.backend.controller.OrderSummaryController;
import com.dinnervery.backend.dto.request.CartAddItemRequest;
import com.dinnervery.backend.dto.request.OrderSummaryRequest;
import com.dinnervery.backend.entity.Address;
import com.dinnervery.backend.entity.Customer;
import com.dinnervery.backend.entity.Menu;
import com.dinnervery.backend.entity.MenuOption;
import com.dinnervery.backend.entity.ServingStyle;
import com.dinnervery.backend.repository.AddressRepository;
import com.dinnervery.backend.repository.CustomerRepository;
import com.dinnervery.backend.repository.MenuRepository;
import com.dinnervery.backend.repository.MenuOptionRepository;
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

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class NewApiTest {

    @Autowired
    private MemberController memberController;

    @Autowired
    private CartController cartController;

    @Autowired
    private OrderSummaryController orderSummaryController;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private MenuRepository menuRepository;

    @Autowired
    private MenuOptionRepository menuOptionRepository;

    @Autowired
    private ServingStyleRepository servingStyleRepository;

    @Autowired
    private AddressRepository addressRepository;

    private Customer customer;
    private Menu menu;
    private MenuOption menuOption;
    private ServingStyle servingStyle;
    private Address address;

    @BeforeEach
    void setUp() {
        // 고객 생성
        customer = Customer.builder()
                .loginId("test_customer_" + Thread.currentThread().getId() + "_" + System.nanoTime())
                .password("password")
                .name("테스트 고객")
                .phoneNumber("010-1234-5678")
                .address("서울시 강남구")
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

        // 메뉴 옵션 생성
        menuOption = MenuOption.builder()
                .menu(menu)
                .itemName("스테이크")
                .itemPrice(15000)
                .defaultQty(1)
                .build();
        menuOption = menuOptionRepository.save(menuOption);

        // 서빙 스타일 생성
        servingStyle = ServingStyle.builder()
                .name("일반")
                .extraPrice(0)
                .build();
        servingStyle = servingStyleRepository.save(servingStyle);
    }

    @Test
    void 영업시간_조회_API_테스트() {
        // 영업시간 조회 API 호출
        ResponseEntity<Map<String, Object>> response = memberController.getBusinessHoursStatus();
        
        // 응답 검증
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        Map<String, Object> responseBody = response.getBody();
        assertThat(responseBody).isNotNull();
        
        // 영업시간 정보 검증
        assertThat(responseBody.get("isOpen")).isNotNull();
        assertThat(responseBody.get("openTime")).isEqualTo("15:30");
        assertThat(responseBody.get("closeTime")).isEqualTo("22:00");
        assertThat(responseBody.get("lastOrderTime")).isEqualTo("21:30");
        assertThat(responseBody.get("message")).isNotNull();
    }

    @Test
    void 주문_내역_확인_API_테스트() {
        // 주문 내역 확인 요청 생성
        OrderSummaryRequest.SelectedOption selectedOption = OrderSummaryRequest.SelectedOption.builder()
                .optionId(menuOption.getId())
                .quantity(2)
                .build();
        
        OrderSummaryRequest request = OrderSummaryRequest.builder()
                .menuId(menu.getId())
                .selectedOptions(List.of(selectedOption))
                .servingStyleId(servingStyle.getId())
                .build();
        
        // 주문 내역 확인 API 호출
        ResponseEntity<Map<String, Object>> response = orderSummaryController.getOrderSummary(request);
        
        // 응답 검증
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        Map<String, Object> responseBody = response.getBody();
        assertThat(responseBody).isNotNull();
        
        // dinnerItems 검증
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> dinnerItems = (List<Map<String, Object>>) responseBody.get("dinnerItems");
        assertThat(dinnerItems).isNotNull().hasSize(1);
        
        Map<String, Object> dinnerItem = dinnerItems.get(0);
        assertThat(dinnerItem.get("menuId")).isEqualTo(menu.getId());
        assertThat(dinnerItem.get("name")).isEqualTo(menu.getName());
        assertThat(dinnerItem.get("quantity")).isEqualTo(1);
        assertThat(dinnerItem.get("unitPrice")).isEqualTo(menu.getPrice());
        
        // options 검증
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> options = (List<Map<String, Object>>) dinnerItem.get("options");
        assertThat(options).isNotNull().hasSize(1);
        
        Map<String, Object> option = options.get(0);
        assertThat(option.get("optionId")).isEqualTo(menuOption.getId());
        assertThat(option.get("name")).isEqualTo(menuOption.getItemName());
        assertThat(option.get("quantity")).isEqualTo(2);
        assertThat(option.get("unitPrice")).isEqualTo(menuOption.getItemPrice());
        
        // servingStyle 검증
        @SuppressWarnings("unchecked")
        Map<String, Object> servingStyleData = (Map<String, Object>) responseBody.get("servingStyle");
        assertThat(servingStyleData).isNotNull();
        assertThat(servingStyleData.get("styleId")).isEqualTo(servingStyle.getId());
        assertThat(servingStyleData.get("name")).isEqualTo(servingStyle.getName());
        assertThat(servingStyleData.get("unitPrice")).isEqualTo(servingStyle.getExtraPrice());
        
        // totalPrice 검증
        int expectedTotal = menu.getPrice() + (menuOption.getItemPrice() * 2) + servingStyle.getExtraPrice();
        assertThat(responseBody.get("totalPrice")).isEqualTo(expectedTotal);
    }

    @Test
    void 장바구니_추가_API_테스트() {
        // 장바구니 추가 요청 생성
        CartAddItemRequest.OptionRequest optionRequest = CartAddItemRequest.OptionRequest.builder()
                .optionId(menuOption.getId())
                .quantity(1)
                .build();
        
        CartAddItemRequest request = CartAddItemRequest.builder()
                .menuId(menu.getId())
                .menuQuantity(2)
                .servingStyleId(servingStyle.getId())
                .options(List.of(optionRequest))
                .build();
        
        // 장바구니 추가 API 호출
        ResponseEntity<Map<String, Object>> response = cartController.addItemToCart(customer.getId(), request);
        
        // 응답 검증
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        Map<String, Object> responseBody = response.getBody();
        assertThat(responseBody).isNotNull();
        
        // 장바구니 아이템 정보 검증
        assertThat(responseBody.get("cartItemId")).isNotNull();
        assertThat(responseBody.get("menuId")).isEqualTo(menu.getId());
        assertThat(responseBody.get("menuName")).isEqualTo(menu.getName());
        assertThat(responseBody.get("quantity")).isEqualTo(2);
        assertThat(responseBody.get("unitPrice")).isEqualTo(menu.getPrice());
        assertThat(responseBody.get("servingStyleId")).isEqualTo(servingStyle.getId());
        assertThat(responseBody.get("servingStyleName")).isEqualTo(servingStyle.getName());
        assertThat(responseBody.get("servingStylePrice")).isEqualTo(servingStyle.getExtraPrice());
        assertThat(responseBody.get("totalPrice")).isEqualTo(menu.getPrice() * 2);
        assertThat(responseBody.get("addedAt")).isNotNull();
    }

    @Test
    void 장바구니_조회_API_테스트() {
        // 먼저 장바구니에 아이템 추가
        CartAddItemRequest request = CartAddItemRequest.builder()
                .menuId(menu.getId())
                .menuQuantity(1)
                .servingStyleId(servingStyle.getId())
                .options(List.of())
                .build();
        
        cartController.addItemToCart(customer.getId(), request);
        
        // 장바구니 조회 API 호출
        ResponseEntity<Map<String, Object>> response = cartController.getCart(customer.getId());
        
        // 응답 검증
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        Map<String, Object> responseBody = response.getBody();
        assertThat(responseBody).isNotNull();
        
        // 장바구니 정보 검증
        assertThat(responseBody.get("cartId")).isNotNull();
        assertThat(responseBody.get("customerId")).isEqualTo(customer.getId());
        
        // cartItems 검증
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> cartItems = (List<Map<String, Object>>) responseBody.get("cartItems");
        assertThat(cartItems).isNotNull().hasSize(1);
        
        Map<String, Object> cartItem = cartItems.get(0);
        assertThat(cartItem.get("cartItemId")).isNotNull();
        
        // dinnerItem 검증
        @SuppressWarnings("unchecked")
        Map<String, Object> dinnerItem = (Map<String, Object>) cartItem.get("dinnerItem");
        assertThat(dinnerItem).isNotNull();
        assertThat(dinnerItem.get("menuId")).isEqualTo(menu.getId());
        assertThat(dinnerItem.get("name")).isEqualTo(menu.getName());
        assertThat(dinnerItem.get("quantity")).isEqualTo(1);
        assertThat(dinnerItem.get("unitPrice")).isEqualTo(menu.getPrice());
        
        // servingStyle 검증
        @SuppressWarnings("unchecked")
        Map<String, Object> servingStyleData = (Map<String, Object>) cartItem.get("servingStyle");
        assertThat(servingStyleData).isNotNull();
        assertThat(servingStyleData.get("styleId")).isEqualTo(servingStyle.getId());
        assertThat(servingStyleData.get("name")).isEqualTo(servingStyle.getName());
        assertThat(servingStyleData.get("price")).isEqualTo(servingStyle.getExtraPrice());
        
        // totalPrice 검증
        assertThat(responseBody.get("totalPrice")).isEqualTo(menu.getPrice());
    }
}
