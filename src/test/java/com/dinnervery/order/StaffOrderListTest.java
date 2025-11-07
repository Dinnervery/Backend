package com.dinnervery.order;

import com.dinnervery.controller.OrderController;
import com.dinnervery.entity.*;
import com.dinnervery.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
@ActiveProfiles("test")
class StaffOrderListTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderRepository orderRepository;

    private Customer customer;
    private Menu menu;
    private Style style;
    private Order requestedOrder;
    private Order cookingOrder;
    private Order cookedOrder;
    private Order deliveringOrder;
    private OrderItem orderItem1;
    private OrderItem orderItem2;
    private MenuOption menuOption;
    private OrderItemOption orderItemOption;

    @BeforeEach
    void setUp() {
        // 고객 생성
        customer = Customer.builder()
                .loginId("test_customer")
                .password("password")
                .name("테스트 고객")
                .phoneNumber("010-1234-5678")
                .address("서울시 강남구 테헤란로 123")
                .build();
        ReflectionTestUtils.setField(customer, "id", 1L);

        // 메뉴 생성
        menu = Menu.builder()
                .name("발렌타인 디너")
                .price(28000)
                .build();
        ReflectionTestUtils.setField(menu, "id", 1L);

        // 스타일 생성
        style = Style.builder()
                .name("SIMPLE")
                .extraPrice(0)
                .build();
        ReflectionTestUtils.setField(style, "id", 1L);

        // 메뉴 옵션 생성
        menuOption = MenuOption.builder()
                .menu(menu)
                .name("스테이크")
                .price(15000)
                .defaultQty(1)
                .build();
        ReflectionTestUtils.setField(menuOption, "id", 1L);

        // REQUESTED 상태 주문
        requestedOrder = Order.builder()
                .customer(customer)
                .address("서울시 강남구 테헤란로 123")
                .cardNumber("1234-5678")
                .deliveryTime(LocalTime.of(20, 0))
                .build();
        ReflectionTestUtils.setField(requestedOrder, "id", 1L);
        orderItem1 = OrderItem.builder()
                .menu(menu)
                .style(style)
                .quantity(1)
                .build();
        requestedOrder.addOrderItem(orderItem1);

        // COOKING 상태 주문
        cookingOrder = Order.builder()
                .customer(customer)
                .address("서울시 강남구 테헤란로 456")
                .cardNumber("5678-9012")
                .deliveryTime(LocalTime.of(21, 0))
                .build();
        ReflectionTestUtils.setField(cookingOrder, "id", 2L);
        cookingOrder.startCooking();
        orderItem2 = OrderItem.builder()
                .menu(menu)
                .style(style)
                .quantity(2)
                .build();
        orderItemOption = OrderItemOption.builder()
                .menuOption(menuOption)
                .quantity(1)
                .build();
        orderItem2.addOrderItemOption(orderItemOption);
        cookingOrder.addOrderItem(orderItem2);

        // COOKED 상태 주문
        cookedOrder = Order.builder()
                .customer(customer)
                .address("서울시 강남구 테헤란로 789")
                .cardNumber("9012-3456")
                .deliveryTime(LocalTime.of(19, 0))
                .build();
        ReflectionTestUtils.setField(cookedOrder, "id", 3L);
        cookedOrder.startCooking();
        cookedOrder.completeCooking();

        // DELIVERING 상태 주문
        deliveringOrder = Order.builder()
                .customer(customer)
                .address("서울시 강남구 테헤란로 321")
                .cardNumber("3456-7890")
                .deliveryTime(LocalTime.of(18, 0))
                .build();
        ReflectionTestUtils.setField(deliveringOrder, "id", 4L);
        deliveringOrder.startCooking();
        deliveringOrder.completeCooking();
        deliveringOrder.startDelivering();
    }

    @Test
    void getCookingOrders_요리목록조회_성공() throws Exception {
        // given - REQUESTED, COOKING 상태 주문만 조회
        when(orderRepository.findByDeliveryStatusIn(anyList()))
                .thenReturn(List.of(requestedOrder, cookingOrder));

        // when & then
        mockMvc.perform(get("/api/orders/cooking")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orders").exists())
                .andExpect(jsonPath("$.orders").isArray())
                .andExpect(jsonPath("$.orders.length()").value(2))
                .andExpect(jsonPath("$.orders[0].orderId").exists())
                .andExpect(jsonPath("$.orders[0].status").exists())
                .andExpect(jsonPath("$.orders[0].deliveryTime").exists())
                .andExpect(jsonPath("$.orders[0].orderedItems").exists());
    }

    @Test
    void getCookingOrders_REQUESTED상태_포함() throws Exception {
        // given
        when(orderRepository.findByDeliveryStatusIn(anyList()))
                .thenReturn(List.of(requestedOrder));

        // when & then
        mockMvc.perform(get("/api/orders/cooking")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orders[0].status").value("REQUESTED"));
    }

    @Test
    void getCookingOrders_COOKING상태_포함() throws Exception {
        // given
        when(orderRepository.findByDeliveryStatusIn(anyList()))
                .thenReturn(List.of(cookingOrder));

        // when & then
        mockMvc.perform(get("/api/orders/cooking")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orders[0].status").value("COOKING"));
    }

    @Test
    void getCookingOrders_주문아이템정보_포함() throws Exception {
        // given
        when(orderRepository.findByDeliveryStatusIn(anyList()))
                .thenReturn(List.of(cookingOrder));

        // when & then
        mockMvc.perform(get("/api/orders/cooking")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orders[0].orderedItems").isArray())
                .andExpect(jsonPath("$.orders[0].orderedItems[0].menuId").exists())
                .andExpect(jsonPath("$.orders[0].orderedItems[0].name").exists())
                .andExpect(jsonPath("$.orders[0].orderedItems[0].quantity").exists())
                .andExpect(jsonPath("$.orders[0].orderedItems[0].styleId").exists())
                .andExpect(jsonPath("$.orders[0].orderedItems[0].styleName").exists())
                .andExpect(jsonPath("$.orders[0].orderedItems[0].options").exists());
    }

    @Test
    void getCookingOrders_옵션정보_포함() throws Exception {
        // given
        when(orderRepository.findByDeliveryStatusIn(anyList()))
                .thenReturn(List.of(cookingOrder));

        // when & then
        mockMvc.perform(get("/api/orders/cooking")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orders[0].orderedItems[0].options").isArray())
                .andExpect(jsonPath("$.orders[0].orderedItems[0].options[0].optionId").exists())
                .andExpect(jsonPath("$.orders[0].orderedItems[0].options[0].name").exists())
                .andExpect(jsonPath("$.orders[0].orderedItems[0].options[0].quantity").exists());
    }

    @Test
    void getCookingOrders_빈목록_반환() throws Exception {
        // given - 조리 대기 주문이 없는 경우
        when(orderRepository.findByDeliveryStatusIn(anyList()))
                .thenReturn(List.of());

        // when & then
        mockMvc.perform(get("/api/orders/cooking")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orders").isArray())
                .andExpect(jsonPath("$.orders.length()").value(0));
    }

    @Test
    void getDeliveryOrders_배달목록조회_성공() throws Exception {
        // given - COOKED, DELIVERING 상태 주문만 조회
        when(orderRepository.findByDeliveryStatusIn(anyList()))
                .thenReturn(List.of(cookedOrder, deliveringOrder));

        // when & then
        mockMvc.perform(get("/api/orders/delivery")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orders").exists())
                .andExpect(jsonPath("$.orders").isArray())
                .andExpect(jsonPath("$.orders.length()").value(2))
                .andExpect(jsonPath("$.orders[0].orderId").exists())
                .andExpect(jsonPath("$.orders[0].status").exists())
                .andExpect(jsonPath("$.orders[0].deliveryTime").exists())
                .andExpect(jsonPath("$.orders[0].address").exists()) // 배달 목록에는 주소가 포함되어야 함
                .andExpect(jsonPath("$.orders[0].orderItems").exists());
    }

    @Test
    void getDeliveryOrders_COOKED상태_포함() throws Exception {
        // given
        when(orderRepository.findByDeliveryStatusIn(anyList()))
                .thenReturn(List.of(cookedOrder));

        // when & then
        mockMvc.perform(get("/api/orders/delivery")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orders[0].status").value("COOKED"))
                .andExpect(jsonPath("$.orders[0].address").value("서울시 강남구 테헤란로 789"));
    }

    @Test
    void getDeliveryOrders_DELIVERING상태_포함() throws Exception {
        // given
        when(orderRepository.findByDeliveryStatusIn(anyList()))
                .thenReturn(List.of(deliveringOrder));

        // when & then
        mockMvc.perform(get("/api/orders/delivery")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orders[0].status").value("DELIVERING"))
                .andExpect(jsonPath("$.orders[0].address").value("서울시 강남구 테헤란로 321"));
    }

    @Test
    void getDeliveryOrders_주소정보_포함() throws Exception {
        // given
        when(orderRepository.findByDeliveryStatusIn(anyList()))
                .thenReturn(List.of(cookedOrder, deliveringOrder));

        // when & then - 배달 목록에는 반드시 주소가 포함되어야 함
        mockMvc.perform(get("/api/orders/delivery")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orders[0].address").value("서울시 강남구 테헤란로 789"))
                .andExpect(jsonPath("$.orders[1].address").value("서울시 강남구 테헤란로 321"));
    }

    @Test
    void getDeliveryOrders_주문아이템정보_포함() throws Exception {
        // given
        when(orderRepository.findByDeliveryStatusIn(anyList()))
                .thenReturn(List.of(cookedOrder));

        // when & then
        mockMvc.perform(get("/api/orders/delivery")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orders[0].orderItems").isArray())
                .andExpect(jsonPath("$.orders[0].orderItems[0].menuId").exists())
                .andExpect(jsonPath("$.orders[0].orderItems[0].name").exists())
                .andExpect(jsonPath("$.orders[0].orderItems[0].quantity").exists())
                .andExpect(jsonPath("$.orders[0].orderItems[0].styleId").exists())
                .andExpect(jsonPath("$.orders[0].orderItems[0].styleName").exists())
                .andExpect(jsonPath("$.orders[0].orderItems[0].options").exists());
    }

    @Test
    void getDeliveryOrders_빈목록_반환() throws Exception {
        // given - 배달 대기 주문이 없는 경우
        when(orderRepository.findByDeliveryStatusIn(anyList()))
                .thenReturn(List.of());

        // when & then
        mockMvc.perform(get("/api/orders/delivery")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orders").isArray())
                .andExpect(jsonPath("$.orders.length()").value(0));
    }

    @Test
    void getCookingOrders_COOKED상태_제외() throws Exception {
        // given - COOKED 상태는 조리 목록에 포함되지 않아야 함
        when(orderRepository.findByDeliveryStatusIn(anyList()))
                .thenReturn(List.of(requestedOrder, cookingOrder)); // COOKED 제외

        // when & then
        mockMvc.perform(get("/api/orders/cooking")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orders.length()").value(2))
                .andExpect(jsonPath("$.orders[?(@.status == 'COOKED')]").doesNotExist());
    }

    @Test
    void getDeliveryOrders_REQUESTED상태_제외() throws Exception {
        // given - REQUESTED 상태는 배달 목록에 포함되지 않아야 함
        when(orderRepository.findByDeliveryStatusIn(anyList()))
                .thenReturn(List.of(cookedOrder, deliveringOrder)); // REQUESTED 제외

        // when & then
        mockMvc.perform(get("/api/orders/delivery")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orders.length()").value(2))
                .andExpect(jsonPath("$.orders[?(@.status == 'REQUESTED')]").doesNotExist());
    }
}

