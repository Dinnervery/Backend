package com.dinnervery.order;

import com.dinnervery.controller.OrderController;
import com.dinnervery.dto.order.response.OrderListResponse;
import com.dinnervery.dto.order.response.DeliveryOrderListResponse;
import com.dinnervery.dto.order.response.OrderUpdateResponse;
import com.dinnervery.entity.Customer;
import com.dinnervery.entity.Order;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(OrderController.class)
@ActiveProfiles("test")
class OrderControllerWebTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private OrderService orderService;

    private Customer customer;
    private Order order;

    @BeforeEach
    void setUp() {
        customer = Customer.builder().loginId("u").password("p").name("고객").phoneNumber("010-0000-0000").address("서울시").build();
        order = Order.builder().customer(customer).address("서울시").cardNumber("1234").deliveryTime(LocalTime.of(20,0)).build();
        ReflectionTestUtils.setField(order, "id", 1L);
    }

    @Test
    void getCookingOrders_returnsOk() throws Exception {
        OrderListResponse response = new OrderListResponse(List.of());
        when(orderService.getCookingOrders()).thenReturn(response);

        mockMvc.perform(get("/api/orders/cooking"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orders").exists());
    }

    @Test
    void getDeliveryOrders_returnsOk() throws Exception {
        DeliveryOrderListResponse response = new DeliveryOrderListResponse(List.of());
        when(orderService.getDeliveryOrders()).thenReturn(response);

        mockMvc.perform(get("/api/orders/delivery"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orders").exists());
    }

    @Test
    void getOrdersByCustomer_returnsOk() throws Exception {
        Map<String, Object> response = new HashMap<>();
        response.put("orders", List.of());
        when(orderService.getOrdersByCustomerIdForResponse(anyLong())).thenReturn(response);

        mockMvc.perform(get("/api/orders/customer/{customerId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orders").exists());
    }

    @Test
    void updateOrderStatus_returnsOk() throws Exception {
        OrderUpdateResponse response = new OrderUpdateResponse(1L, "COOKING");
        Map<String, Object> request = new HashMap<>();
        request.put("status", "COOKING");
        when(orderService.updateOrderStatus(anyLong(), request)).thenReturn(response);

        mockMvc.perform(patch("/api/orders/{id}/status", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").exists());
    }
}


