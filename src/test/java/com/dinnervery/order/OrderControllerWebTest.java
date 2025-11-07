package com.dinnervery.order;

import com.dinnervery.controller.OrderController;
import com.dinnervery.dto.order.request.PriceCalculationRequest;
import com.dinnervery.entity.*;
import com.dinnervery.repository.*;
import com.dinnervery.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(OrderController.class)
@ActiveProfiles("test")
class OrderControllerWebTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private OrderService orderService;
    @MockitoBean private OrderRepository orderRepository;
    @MockitoBean private OrderItemRepository orderItemRepository;
    @MockitoBean private CustomerRepository customerRepository;
    @MockitoBean private MenuRepository menuRepository;
    @MockitoBean private StyleRepository styleRepository;
    @MockitoBean private MenuOptionRepository menuOptionRepository;

    private Customer customer;
    private Menu menu;
    private Style style;

    @BeforeEach
    void setUp() {
        customer = Customer.builder().loginId("u").password("p").name("고객").phoneNumber("010-0000-0000").address("서울시").build();
        menu = Menu.builder().name("메뉴").price(10000).build();
        style = Style.builder().name("기본").extraPrice(0).build();

        when(customerRepository.findById(anyLong())).thenReturn(Optional.of(customer));
        when(menuRepository.findById(anyLong())).thenReturn(Optional.of(menu));
        when(styleRepository.findById(anyLong())).thenReturn(Optional.of(style));
        Order orderForDetail = Order.builder().customer(customer).address("서울시").cardNumber("1234").deliveryTime(LocalTime.of(20,0)).build();
        ReflectionTestUtils.setField(orderForDetail, "id", 1L);
        when(orderRepository.findByIdWithDetails(anyLong())).thenReturn(Optional.of(orderForDetail));
        when(orderRepository.findByCustomerIdWithDetails(anyLong())).thenReturn(List.of(
                Order.builder().customer(customer).address("서울시").cardNumber("1234").deliveryTime(LocalTime.of(20,0)).build()
        ));
    }

    @Test
    void calculatePrice_returnsOk() throws Exception {
        PriceCalculationRequest.OrderItemRequest item = PriceCalculationRequest.OrderItemRequest.builder()
                .menuId(1L).quantity(1).styleId(1L).optionIds(List.of()).build();
        PriceCalculationRequest req = PriceCalculationRequest.builder()
                .customerId(1L).items(List.of(item)).build();

        mockMvc.perform(post("/api/orders/calculate-price")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subtotal").exists());
    }

    @Test
    void getOrderDetail_returnsOk() throws Exception {
        mockMvc.perform(get("/api/orders/detail/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").exists());
    }

    @Test
    void getOrdersByCustomer_returnsOk() throws Exception {
        mockMvc.perform(get("/api/customers/{id}/orders", 1L))
                .andExpect(status().isOk());
    }
}


