package com.dinnervery.order;

import com.dinnervery.controller.OrderController;
import com.dinnervery.dto.order.request.OrderCreateRequest;
import com.dinnervery.entity.*;
import com.dinnervery.repository.*;
import com.dinnervery.service.OrderService;
import com.dinnervery.service.StorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.util.ReflectionTestUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
@ActiveProfiles("test")
class OrderCreateIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private OrderRepository orderRepository;

    @MockitoBean
    private CustomerRepository customerRepository;

    @MockitoBean
    private MenuRepository menuRepository;

    @MockitoBean
    private StyleRepository styleRepository;

    @MockitoBean
    private MenuOptionRepository menuOptionRepository;

    @MockitoBean
    private StorageService storageService;

    @MockitoBean
    private CartRepository cartRepository;

    private Customer customer;
    private Style simpleStyle;
    private Menu valentineMenu;
    private Menu englishMenu;
    private Menu frenchMenu;
    private Menu champagneMenu;
    private MenuOption steakOption;
    private MenuOption wineOption;
    private Storage meatStorage;
    private Storage wineStorage;

    @BeforeEach
    void setUp() {
        // 고객 생성
        customer = Customer.builder()
                .loginId("test_customer")
                .password("password")
                .name("테스트 고객")
                .phoneNumber("010-1234-5678")
                .address("서울시 강남구")
                .build();
        ReflectionTestUtils.setField(customer, "id", 1L);

        // 스타일 생성
        simpleStyle = Style.builder()
                .name("SIMPLE")
                .extraPrice(0)
                .build();
        ReflectionTestUtils.setField(simpleStyle, "id", 1L);

        // 4대 디너 메뉴 생성
        valentineMenu = Menu.builder()
                .name("발렌타인 디너")
                .price(28000)
                .build();
        ReflectionTestUtils.setField(valentineMenu, "id", 1L);

        englishMenu = Menu.builder()
                .name("잉글리시 디너")
                .price(35000)
                .build();
        ReflectionTestUtils.setField(englishMenu, "id", 2L);

        frenchMenu = Menu.builder()
                .name("프렌치 디너")
                .price(45000)
                .build();
        ReflectionTestUtils.setField(frenchMenu, "id", 3L);

        champagneMenu = Menu.builder()
                .name("샴페인 축제 디너")
                .price(90000)
                .build();
        ReflectionTestUtils.setField(champagneMenu, "id", 4L);

        // 재고 생성
        meatStorage = Storage.builder()
                .name("고기")
                .quantity(100)
                .build();
        ReflectionTestUtils.setField(meatStorage, "id", 1L);

        wineStorage = Storage.builder()
                .name("와인")
                .quantity(100)
                .build();
        ReflectionTestUtils.setField(wineStorage, "id", 2L);

        // 옵션 생성
        steakOption = MenuOption.builder()
                .menu(valentineMenu)
                .name("스테이크")
                .price(15000)
                .defaultQty(1)
                .build();
        steakOption.setStorageItem(meatStorage);
        steakOption.setStorageConsumption(1);
        ReflectionTestUtils.setField(steakOption, "id", 1L);

        wineOption = MenuOption.builder()
                .menu(valentineMenu)
                .name("와인")
                .price(8000)
                .defaultQty(1)
                .build();
        wineOption.setStorageItem(wineStorage);
        wineOption.setStorageConsumption(1);
        ReflectionTestUtils.setField(wineOption, "id", 2L);

        // Mock 설정
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(styleRepository.findById(1L)).thenReturn(Optional.of(simpleStyle));
        when(menuRepository.findById(1L)).thenReturn(Optional.of(valentineMenu));
        when(menuRepository.findById(2L)).thenReturn(Optional.of(englishMenu));
        when(menuRepository.findById(3L)).thenReturn(Optional.of(frenchMenu));
        when(menuRepository.findById(4L)).thenReturn(Optional.of(champagneMenu));
        when(menuOptionRepository.findById(1L)).thenReturn(Optional.of(steakOption));
        when(menuOptionRepository.findById(2L)).thenReturn(Optional.of(wineOption));

        // OrderService mock 응답
        Order order = Order.builder()
                .customer(customer)
                .address("서울시 강남구")
                .cardNumber("1234-5678")
                .deliveryTime(LocalTime.of(20, 0))
                .build();
        ReflectionTestUtils.setField(order, "id", 1L);

        when(orderService.createOrder(any(OrderCreateRequest.class))).thenAnswer(invocation -> {
            return com.dinnervery.dto.order.response.OrderResponse.from(order);
        });
    }

    @Test
    void createOrder_발렌타인디너_성공() throws Exception {
        // given - Cart와 CartItem 모킹
        Cart cart = Cart.builder().customer(customer).build();
        ReflectionTestUtils.setField(cart, "id", 1L);
        
        CartItem cartItem = CartItem.builder()
                .menu(valentineMenu)
                .style(simpleStyle)
                .quantity(1)
                .build();
        ReflectionTestUtils.setField(cartItem, "id", 1L);
        cart.addCartItem(cartItem);
        
        when(cartRepository.findByCustomer_Id(1L)).thenReturn(Optional.of(cart));

        OrderCreateRequest request = OrderCreateRequest.builder()
                .customerId(1L)
                .address("서울시 강남구")
                .cardNumber("1234-5678")
                .deliveryTime(LocalTime.of(20, 0))
                .build();

        // when & then
        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId").exists());
    }

    @Test
    void createOrder_잉글리시디너_성공() throws Exception {
        // given - Cart와 CartItem 모킹
        Cart cart = Cart.builder().customer(customer).build();
        ReflectionTestUtils.setField(cart, "id", 1L);
        
        CartItem cartItem = CartItem.builder()
                .menu(englishMenu)
                .style(simpleStyle)
                .quantity(1)
                .build();
        ReflectionTestUtils.setField(cartItem, "id", 1L);
        cart.addCartItem(cartItem);
        
        when(cartRepository.findByCustomer_Id(1L)).thenReturn(Optional.of(cart));

        OrderCreateRequest request = OrderCreateRequest.builder()
                .customerId(1L)
                .address("서울시 강남구")
                .cardNumber("1234-5678")
                .deliveryTime(LocalTime.of(20, 0))
                .build();

        // when & then
        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId").exists());
    }

    @Test
    void createOrder_프렌치디너_성공() throws Exception {
        // given - Cart와 CartItem 모킹
        Cart cart = Cart.builder().customer(customer).build();
        ReflectionTestUtils.setField(cart, "id", 1L);
        
        CartItem cartItem = CartItem.builder()
                .menu(frenchMenu)
                .style(simpleStyle)
                .quantity(1)
                .build();
        ReflectionTestUtils.setField(cartItem, "id", 1L);
        cart.addCartItem(cartItem);
        
        when(cartRepository.findByCustomer_Id(1L)).thenReturn(Optional.of(cart));

        OrderCreateRequest request = OrderCreateRequest.builder()
                .customerId(1L)
                .address("서울시 강남구")
                .cardNumber("1234-5678")
                .deliveryTime(LocalTime.of(20, 0))
                .build();

        // when & then
        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId").exists());
    }

    @Test
    void createOrder_샴페인축제디너_성공() throws Exception {
        // given - Cart와 CartItem 모킹
        Cart cart = Cart.builder().customer(customer).build();
        ReflectionTestUtils.setField(cart, "id", 1L);
        
        CartItem cartItem = CartItem.builder()
                .menu(champagneMenu)
                .style(simpleStyle)
                .quantity(1)
                .build();
        ReflectionTestUtils.setField(cartItem, "id", 1L);
        cart.addCartItem(cartItem);
        
        when(cartRepository.findByCustomer_Id(1L)).thenReturn(Optional.of(cart));

        OrderCreateRequest request = OrderCreateRequest.builder()
                .customerId(1L)
                .address("서울시 강남구")
                .cardNumber("1234-5678")
                .deliveryTime(LocalTime.of(20, 0))
                .build();

        // when & then
        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId").exists());
    }

    @Test
    void createOrder_재고확인없이_주문생성() throws Exception {
        // given - Cart와 CartItem 모킹
        Cart cart = Cart.builder().customer(customer).build();
        ReflectionTestUtils.setField(cart, "id", 1L);
        
        CartItem cartItem = CartItem.builder()
                .menu(valentineMenu)
                .style(simpleStyle)
                .quantity(1)
                .build();
        ReflectionTestUtils.setField(cartItem, "id", 1L);
        
        CartItemOption cartItemOption = CartItemOption.builder()
                .menuOption(steakOption)
                .quantity(1)
                .build();
        cartItem.addCartItemOption(cartItemOption);
        cart.addCartItem(cartItem);
        
        when(cartRepository.findByCustomer_Id(1L)).thenReturn(Optional.of(cart));

        OrderCreateRequest request = OrderCreateRequest.builder()
                .customerId(1L)
                .address("서울시 강남구")
                .cardNumber("1234-5678")
                .deliveryTime(LocalTime.of(20, 0))
                .build();

        // when & then - 주문 생성은 재고 확인 없이 성공해야 함
        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Note: 주문 생성 시점에는 재고 확인/차감을 하지 않음
        // 재고 확인/차감은 COOKED 상태 변경 시점에 수행됨
    }
}

