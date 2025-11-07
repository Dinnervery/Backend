package com.dinnervery.order;

import com.dinnervery.controller.AuthController;
import com.dinnervery.dto.auth.request.LoginRequest;
import com.dinnervery.dto.auth.request.SignupRequest;
import com.dinnervery.entity.Customer;
import com.dinnervery.entity.Staff;
import com.dinnervery.repository.CustomerRepository;
import com.dinnervery.repository.StaffRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(AuthController.class)
@ActiveProfiles("test")
class AuthControllerWebTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private CustomerRepository customerRepository;
    @MockitoBean private StaffRepository staffRepository;

    private Customer customer;
    private Staff staff;

    @BeforeEach
    void setUp() {
        customer = Customer.builder()
                .loginId("cust1").password("pw").name("고객1").phoneNumber("010-1111-2222").address("서울시 강남구").build();
        staff = Staff.builder()
                .loginId("emp1").password("pw").name("직원1").task(Staff.StaffTask.COOK).build();

        when(customerRepository.existsByLoginId("cust1")).thenReturn(false);
        when(customerRepository.findByLoginId("cust1")).thenReturn(Optional.of(customer));
        when(customerRepository.findById(any(Long.class))).thenReturn(Optional.of(customer));
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);

        when(staffRepository.findByLoginId("emp1")).thenReturn(Optional.of(staff));
    }

    @Test
    void customerSignup_returnsOk() throws Exception {
        SignupRequest req = SignupRequest.builder()
                .loginId("cust1").password("pw").name("고객1").phoneNumber("010-1111-2222").address("서울시 강남구").build();

        mockMvc.perform(post("/api/auth/customer/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.loginId").value("cust1"));
    }

    @Test
    void customerLogin_returnsOk() throws Exception {
        LoginRequest req = LoginRequest.builder().loginId("cust1").password("pw").build();

        mockMvc.perform(post("/api/auth/customer/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.loginId").value("cust1"));
    }

    @Test
    void staffLogin_returnsOk() throws Exception {
        LoginRequest req = LoginRequest.builder().loginId("emp1").password("pw").build();

        mockMvc.perform(post("/api/auth/staff/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.loginId").value("emp1"));
    }

    @Test
    void getCustomer_returnsOk() throws Exception {
        mockMvc.perform(get("/api/auth/customers/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.loginId").value("cust1"));
    }
}


