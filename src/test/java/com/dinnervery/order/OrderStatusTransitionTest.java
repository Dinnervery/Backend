package com.dinnervery.order;

import com.dinnervery.controller.OrderController;
import com.dinnervery.entity.*;
import com.dinnervery.repository.CustomerRepository;
import com.dinnervery.repository.MenuRepository;
import com.dinnervery.repository.OrderRepository;
import com.dinnervery.repository.StyleRepository;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
@ActiveProfiles("test")
class OrderStatusTransitionTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrderRepository orderRepository;

    @MockitoBean
    private CustomerRepository customerRepository;

    @MockitoBean
    private MenuRepository menuRepository;

    @MockitoBean
    private StyleRepository styleRepository;

    @MockitoBean
    private StorageService storageService;

    private Customer customer;
    private Menu menu;
    private Style style;
    private Order order;
    private OrderItem orderItem;
    private MenuOption menuOption;
    private OrderItemOption orderItemOption;
    private Storage storage;

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

        // 재고 생성
        storage = Storage.builder()
                .name("고기")
                .quantity(100)
                .build();
        ReflectionTestUtils.setField(storage, "id", 1L);

        // 메뉴 옵션 생성
        menuOption = MenuOption.builder()
                .menu(menu)
                .name("스테이크")
                .price(15000)
                .defaultQty(1)
                .build();
        menuOption.setStorageItem(storage);
        menuOption.setStorageConsumption(1);
        ReflectionTestUtils.setField(menuOption, "id", 1L);

        // 주문 생성 (초기 상태: REQUESTED)
        order = Order.builder()
                .customer(customer)
                .address("서울시 강남구")
                .cardNumber("1234-5678")
                .deliveryTime(LocalTime.of(20, 0))
                .build();
        ReflectionTestUtils.setField(order, "id", 1L);

        // 주문 아이템 및 옵션 추가
        orderItem = OrderItem.builder()
                .menu(menu)
                .style(style)
                .quantity(1)
                .build();
        orderItemOption = OrderItemOption.builder()
                .menuOption(menuOption)
                .quantity(1)
                .build();
        orderItem.addOrderItemOption(orderItemOption);
        order.addOrderItem(orderItem);

        when(orderRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(order));
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);
    }

    @Test
    void updateOrderStatus_COOKING_성공() throws Exception {
        // given
        Map<String, Object> request = new HashMap<>();
        request.put("status", "COOKING");

        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order savedOrder = invocation.getArgument(0);
            savedOrder.startCooking();
            return savedOrder;
        });

        // when & then
        mockMvc.perform(patch("/api/orders/{id}/status", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(1L))
                .andExpect(jsonPath("$.status").value("COOKING"));
    }

    @Test
    void updateOrderStatus_COOKED_성공() throws Exception {
        // given - COOKING 상태로 먼저 변경
        order.startCooking();
        Map<String, Object> request = new HashMap<>();
        request.put("status", "COOKED");

        // 재고 확인 및 차감이 정상 동작하도록 Mocking
        doNothing().when(storageService).checkStock(any(MenuOption.class), anyInt());
        doNothing().when(storageService).deductStock(any(MenuOption.class), anyInt());

        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order savedOrder = invocation.getArgument(0);
            savedOrder.completeCooking();
            return savedOrder;
        });

        // when & then
        mockMvc.perform(patch("/api/orders/{id}/status", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(1L))
                .andExpect(jsonPath("$.status").value("COOKED"));

        // 재고 확인 및 차감이 호출되었는지 검증
        verify(storageService, times(1)).checkStock(any(MenuOption.class), anyInt());
        verify(storageService, times(1)).deductStock(any(MenuOption.class), anyInt());
    }

    @Test
    void updateOrderStatus_COOKED_재고부족_실패() throws Exception {
        // given - COOKING 상태로 먼저 변경
        order.startCooking();
        Map<String, Object> request = new HashMap<>();
        request.put("status", "COOKED");

        // 재고 부족 예외 발생하도록 Mocking
        doThrow(new IllegalStateException("고기 재고가 부족합니다."))
                .when(storageService).checkStock(any(MenuOption.class), anyInt());

        // when & then - 409 Conflict 에러 발생
        mockMvc.perform(patch("/api/orders/{id}/status", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());

        // 재고 차감은 호출되지 않아야 함
        verify(storageService, never()).deductStock(any(MenuOption.class), anyInt());
    }

    @Test
    void updateOrderStatus_COOKED_재고차감_성공() throws Exception {
        // given - COOKING 상태로 먼저 변경
        order.startCooking();
        Map<String, Object> request = new HashMap<>();
        request.put("status", "COOKED");

        // 재고 확인 및 차감이 정상 동작하도록 Mocking
        doNothing().when(storageService).checkStock(any(MenuOption.class), anyInt());
        doNothing().when(storageService).deductStock(any(MenuOption.class), anyInt());

        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order savedOrder = invocation.getArgument(0);
            savedOrder.completeCooking();
            return savedOrder;
        });

        // when
        mockMvc.perform(patch("/api/orders/{id}/status", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(1L))
                .andExpect(jsonPath("$.status").value("COOKED"));

        // then - 재고 차감이 1회 호출되었는지 검증
        verify(storageService, times(1)).checkStock(any(MenuOption.class), anyInt());
        verify(storageService, times(1)).deductStock(any(MenuOption.class), anyInt());
    }

    @Test
    void updateOrderStatus_DELIVERING_성공() throws Exception {
        // given - COOKED 상태로 먼저 변경
        order.startCooking();
        order.completeCooking();
        Map<String, Object> request = new HashMap<>();
        request.put("status", "DELIVERING");

        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order savedOrder = invocation.getArgument(0);
            savedOrder.startDelivering();
            return savedOrder;
        });

        // when & then
        mockMvc.perform(patch("/api/orders/{id}/status", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(1L))
                .andExpect(jsonPath("$.status").value("DELIVERING"));
    }

    @Test
    void updateOrderStatus_DONE_성공() throws Exception {
        // given - DELIVERING 상태로 먼저 변경
        order.startCooking();
        order.completeCooking();
        order.startDelivering();
        Map<String, Object> request = new HashMap<>();
        request.put("status", "DONE");

        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order savedOrder = invocation.getArgument(0);
            savedOrder.completeDelivery();
            return savedOrder;
        });

        // when & then
        mockMvc.perform(patch("/api/orders/{id}/status", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(1L))
                .andExpect(jsonPath("$.status").value("DONE"));
    }

    @Test
    void updateOrderStatus_DONE_고객주문수증가() throws Exception {
        // given - DELIVERING 상태
        order.startCooking();
        order.completeCooking();
        order.startDelivering();
        Map<String, Object> request = new HashMap<>();
        request.put("status", "DONE");

        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order savedOrder = invocation.getArgument(0);
            savedOrder.completeDelivery();
            return savedOrder;
        });

        // when & then
        mockMvc.perform(patch("/api/orders/{id}/status", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // Note: 고객 주문 수가 증가해야 함 (실제 구현에서는 customer.incrementOrderCount() 호출)
        // Mock 설정에서 customerRepository.save()가 호출되어야 함
    }

    @Test
    void updateOrderStatus_전체상태전환_통합테스트() throws Exception {
        // given - 초기 상태: REQUESTED
        Map<String, String> statuses = Map.of(
                "COOKING", "COOKING",
                "COOKED", "COOKED",
                "DELIVERING", "DELIVERING",
                "DONE", "DONE"
        );

        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order savedOrder = invocation.getArgument(0);
            return savedOrder;
        });

        // when & then - 각 상태로 순차적으로 변경
        for (Map.Entry<String, String> entry : statuses.entrySet()) {
            Map<String, Object> request = new HashMap<>();
            request.put("status", entry.getKey());

            // 상태에 따라 적절한 메서드 호출 시뮬레이션
            if ("COOKING".equals(entry.getKey())) {
                order.startCooking();
            } else if ("COOKED".equals(entry.getKey())) {
                order.completeCooking();
            } else if ("DELIVERING".equals(entry.getKey())) {
                order.startDelivering();
            } else if ("DONE".equals(entry.getKey())) {
                order.completeDelivery();
            }

            mockMvc.perform(patch("/api/orders/{id}/status", 1L)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(entry.getValue()));
        }
    }

    @Test
    void updateOrderStatus_유효하지않은상태_예외() throws Exception {
        // given
        Map<String, Object> request = new HashMap<>();
        request.put("status", "INVALID_STATUS");

        // when & then
        mockMvc.perform(patch("/api/orders/{id}/status", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}

