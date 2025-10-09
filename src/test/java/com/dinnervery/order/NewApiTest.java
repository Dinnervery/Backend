package com.dinnervery.order;

import com.dinnervery.controller.CartController;
import com.dinnervery.controller.BusinessHoursController;
import com.dinnervery.controller.OrderSummaryController;
import com.dinnervery.dto.request.CartAddItemRequest;
import com.dinnervery.dto.request.OrderSummaryRequest;
import com.dinnervery.entity.Address;
import com.dinnervery.entity.Customer;
import com.dinnervery.entity.Menu;
import com.dinnervery.entity.MenuOption;
import com.dinnervery.entity.ServingStyle;
import com.dinnervery.repository.AddressRepository;
import com.dinnervery.repository.CustomerRepository;
import com.dinnervery.repository.MenuRepository;
import com.dinnervery.repository.MenuOptionRepository;
import com.dinnervery.repository.ServingStyleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class NewApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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
        // given - 테스트 데이터 생성
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

        // 메뉴 옵션 생성
        menuOption = MenuOption.builder()
                .menu(menu)
                .itemName("와인세트")
                .itemPrice(15000)
                .defaultQty(1)
                .build();
        menuOption = menuOptionRepository.save(menuOption);

        // 서빙 스타일 생성
        servingStyle = ServingStyle.builder()
                .name("기본_" + System.currentTimeMillis())
                .extraPrice(0)
                .build();
        servingStyle = servingStyleRepository.save(servingStyle);
    }

    @Test
    @org.junit.jupiter.api.Order(1)
    void testBusinessHoursRetrievalAPI() throws Exception {
        // when & then - 영업시간 조회 API 호출
        mockMvc.perform(get("/api/business-hours/status")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.openTime").value("15:30"))
                .andExpect(jsonPath("$.closeTime").value("22:00"))
                .andExpect(jsonPath("$.lastOrderTime").value("21:30"));
    }

    @Test
    @org.junit.jupiter.api.Order(2)
    void testOrderSummaryConfirmationAPI() throws Exception {
        // given - 간단한 주문 요약 확인 요청 생성
        OrderSummaryRequest.SelectedOption selectedOption = OrderSummaryRequest.SelectedOption.builder()
                .optionId(menuOption.getId())
                .quantity(1)
                .build();
        
        OrderSummaryRequest request = OrderSummaryRequest.builder()
                .menuId(menu.getId())
                .selectedOptions(List.of(selectedOption))
                .servingStyleId(servingStyle.getId())
                .build();
        
        // when & then - 주문 요약 확인 API 호출
        mockMvc.perform(post("/api/order-summary")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPrice").exists())
                .andExpect(jsonPath("$.dinnerItems").exists());
    }

    @Test
    @org.junit.jupiter.api.Order(3)
    void testCartAddAPI() throws Exception {
        // given - 간단한 장바구니 추가 요청 생성
        CartAddItemRequest request = CartAddItemRequest.builder()
                .menuId(menu.getId())
                .menuQuantity(1)
                .servingStyleId(servingStyle.getId())
                .options(List.of())
                .build();
        
        // when & then - 장바구니 추가 API 호출
        mockMvc.perform(post("/api/cart/{customerId}/add", customer.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cartItemId").exists())
                .andExpect(jsonPath("$.menuId").value(menu.getId()));
    }

    @Test
    @org.junit.jupiter.api.Order(4)
    void testCartRetrievalAPI() throws Exception {
        // given - 먼저 장바구니에 아이템 추가
        CartAddItemRequest request = CartAddItemRequest.builder()
                .menuId(menu.getId())
                .menuQuantity(1)
                .servingStyleId(servingStyle.getId())
                .options(List.of())
                .build();
        
        mockMvc.perform(post("/api/cart/{customerId}/add", customer.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
        
        // when & then - 장바구니 조회 API 호출
        mockMvc.perform(get("/api/cart/{customerId}", customer.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cartId").exists())
                .andExpect(jsonPath("$.customerId").value(customer.getId()));
    }
}
