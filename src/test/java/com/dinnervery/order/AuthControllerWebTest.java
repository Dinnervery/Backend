package com.dinnervery.order;

import com.dinnervery.controller.AuthController;
import com.dinnervery.dto.auth.request.LoginRequest;
import com.dinnervery.dto.auth.request.SignupRequest;
import com.dinnervery.dto.auth.response.LoginResponse;
import com.dinnervery.dto.auth.response.StaffAuthResponse;
import com.dinnervery.dto.customer.response.CustomerResponse;
import com.dinnervery.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(AuthController.class)
@ActiveProfiles("test")
class AuthControllerWebTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private AuthService authService;

    @BeforeEach
    void setUp() {
        // Mock 설정
    }

    @Test
    void customerSignup_returnsOk() throws Exception {
        SignupRequest req = SignupRequest.builder()
                .loginId("cust1").password("pw").name("고객1").phoneNumber("010-1111-2222").address("서울시 강남구").build();

        CustomerResponse response = new CustomerResponse(1L, "cust1", "고객1", "010-1111-2222", "BRONZE", 0);
        when(authService.customerSignup(any(SignupRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/customer/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.loginId").value("cust1"));
    }

    @Test
    void customerLogin_returnsOk() throws Exception {
        LoginRequest req = LoginRequest.builder().loginId("cust1").password("pw").build();

        LoginResponse response = new LoginResponse(1L, "cust1", "고객1", "BRONZE", "token");
        when(authService.customerLogin(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/customer/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.loginId").value("cust1"));
    }

    @Test
    void staffLogin_returnsOk() throws Exception {
        LoginRequest req = LoginRequest.builder().loginId("emp1").password("pw").build();

        StaffAuthResponse response = new StaffAuthResponse(1L, "emp1", "직원1", "COOK", "token");
        when(authService.staffLogin(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/staff/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.loginId").value("emp1"));
    }

}


