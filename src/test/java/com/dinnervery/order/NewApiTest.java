package com.dinnervery.order;

import com.dinnervery.controller.CartController;
import com.dinnervery.dto.cart.request.CartAddItemRequest;
import com.dinnervery.entity.Customer;
import com.dinnervery.entity.Menu;
import com.dinnervery.entity.MenuOption;
import com.dinnervery.entity.Style;
import com.dinnervery.entity.Cart;
import com.dinnervery.entity.CartItem;
import com.dinnervery.entity.CartItemOption;
import com.dinnervery.repository.CustomerRepository;
import com.dinnervery.repository.MenuRepository;
import com.dinnervery.repository.MenuOptionRepository;
import com.dinnervery.repository.StyleRepository;
import com.dinnervery.repository.CartRepository;
import com.dinnervery.repository.CartItemRepository;
import com.dinnervery.repository.CartItemOptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.ArrayList;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

@WebMvcTest(controllers = {
    CartController.class
})
@ActiveProfiles("test")
class NewApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean private CustomerRepository customerRepository;
    @MockitoBean private MenuRepository menuRepository;
    @MockitoBean private MenuOptionRepository menuOptionRepository;
    @MockitoBean private StyleRepository styleRepository;
    @MockitoBean private CartRepository cartRepository;
    @MockitoBean private CartItemRepository cartItemRepository;
    @MockitoBean private CartItemOptionRepository cartItemOptionRepository;

    // ID 상수
    private final Long MOCK_CUSTOMER_ID = 1L;
    private final Long MOCK_MENU_ID = 10L;
    private final Long MOCK_OPTION_ID = 100L;
    private final Long MOCK_STYLE_ID = 20L;
    private final Long MOCK_CART_ID = 5L;
    private final Long MOCK_CART_ITEM_ID = 50L;

    private Customer customer;
    private Menu menu;
    private MenuOption menuOption;
    private Style style;
    private Cart cart;

    @BeforeEach
    void setUp() {
        // given - 테스트 데이터 생성 (모두 mock stub)
        customer = Customer.builder()
                .loginId("test_customer_" + System.currentTimeMillis())
                .password("password")
                .name("테스트 고객")
                .phoneNumber("010-1234-5678")
                .address("서울시 강남구 테헤란로 123")
                .build();
        ReflectionTestUtils.setField(customer, "id", MOCK_CUSTOMER_ID);
        when(customerRepository.findById(MOCK_CUSTOMER_ID)).thenReturn(Optional.of(customer));

        menu = Menu.builder()
                .name("발렌타인 디너")
                .price(28000)
                .build();
        ReflectionTestUtils.setField(menu, "id", MOCK_MENU_ID);
        when(menuRepository.findById(MOCK_MENU_ID)).thenReturn(Optional.of(menu));

        menuOption = MenuOption.builder()
                .menu(menu)
                .name("와인세트")
                .price(15000)
                .defaultQty(1)
                .build();
        ReflectionTestUtils.setField(menuOption, "id", MOCK_OPTION_ID);
        when(menuOptionRepository.findById(MOCK_OPTION_ID)).thenReturn(Optional.of(menuOption));

        style = Style.builder()
                .name("기본_" + System.currentTimeMillis())
                .extraPrice(0)
                .build();
        ReflectionTestUtils.setField(style, "id", MOCK_STYLE_ID);
        when(styleRepository.findById(MOCK_STYLE_ID)).thenReturn(Optional.of(style));

        // Cart stubs
        cart = Cart.builder().customer(customer).build();
        when(cartRepository.findByCustomer_Id(MOCK_CUSTOMER_ID)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCart_Id(MOCK_CART_ID)).thenReturn(new ArrayList<>());
        when(cartRepository.save(any())).thenAnswer(invocation -> {
            Cart c = invocation.getArgument(0);
            // save 시 id 부여 시뮬레이션
            ReflectionTestUtils.setField(c, "id", MOCK_CART_ID);
            if (c != null) {
                // 장바구니에 아이템이 있으면 첫 아이템 id도 세팅
                try {
                    java.lang.reflect.Field itemsField = c.getClass().getDeclaredField("cartItems");
                    itemsField.setAccessible(true);
                    @SuppressWarnings("unchecked")
                    java.util.List<CartItem> items = (java.util.List<CartItem>) itemsField.get(c);
                    if (items != null && !items.isEmpty()) {
                        ReflectionTestUtils.setField(items.get(0), "id", MOCK_CART_ITEM_ID);
                    }
                } catch (Exception ignored) { }
            }
            return c;
        });

    }

    @Test
    @org.junit.jupiter.api.Order(1)
    void testCartAddAPI() throws Exception {
        CartAddItemRequest request = CartAddItemRequest.builder()
                .menuId(MOCK_MENU_ID)
                .menuQuantity(1)
                .styleId(MOCK_STYLE_ID)
                .options(List.of())
                .build();
        
        mockMvc.perform(post("/api/cart/{customerId}/items", MOCK_CUSTOMER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cartItemId").exists())
                .andExpect(jsonPath("$.menu.menuId").value(MOCK_MENU_ID));
    }

    @Test
    @org.junit.jupiter.api.Order(2)
    void testCartRetrievalAPI() throws Exception {
        // cart와 cartItems가 존재하는 시나리오
        ReflectionTestUtils.setField(cart, "id", MOCK_CART_ID);
        CartItem item = CartItem.builder().menu(menu).style(style).quantity(1).build();
        ReflectionTestUtils.setField(item, "id", MOCK_CART_ITEM_ID);
        when(cartItemRepository.findByCart_Id(MOCK_CART_ID)).thenReturn(java.util.List.of(item));
        
        mockMvc.perform(get("/api/cart/{customerId}", MOCK_CUSTOMER_ID)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cartId").value(MOCK_CART_ID))
                .andExpect(jsonPath("$.customerId").value(MOCK_CUSTOMER_ID));
    }

    @Test
    @org.junit.jupiter.api.Order(3)
    void testChangeOptionQuantityAPI() throws Exception {
        Cart cartLocal = Cart.builder().customer(customer).build();
        ReflectionTestUtils.setField(cartLocal, "id", MOCK_CART_ID);
        
        CartItem item = CartItem.builder().menu(menu).style(style).quantity(1).build();
        ReflectionTestUtils.setField(item, "id", MOCK_CART_ITEM_ID);
        cartLocal.addCartItem(item);
        
        CartItemOption cartItemOption = CartItemOption.builder()
                .menuOption(menuOption)
                .quantity(1)
                .build();
        item.addCartItemOption(cartItemOption);
        
        when(cartItemRepository.findById(MOCK_CART_ITEM_ID)).thenReturn(Optional.of(item));
        when(menuOptionRepository.findById(MOCK_OPTION_ID)).thenReturn(Optional.of(menuOption));
        when(cartItemOptionRepository.findByCartItem_IdAndMenuOption_Id(MOCK_CART_ITEM_ID, MOCK_OPTION_ID))
                .thenReturn(Optional.of(cartItemOption));
        when(cartRepository.findByCustomer_Id(MOCK_CUSTOMER_ID)).thenReturn(Optional.of(cartLocal));
        when(cartItemRepository.findByCart_Id(MOCK_CART_ID)).thenReturn(List.of(item));
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(item);
        
        String payload = "{\"quantity\":2}";
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .patch("/api/cart/{customerId}/items/{cartItemId}/options/{optionId}", MOCK_CUSTOMER_ID, MOCK_CART_ITEM_ID, MOCK_OPTION_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cartItemId").value(MOCK_CART_ITEM_ID));
    }

}
