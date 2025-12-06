package com.dinnervery.controller;

import com.dinnervery.dto.auth.request.SignupRequest;
import com.dinnervery.dto.auth.request.LoginRequest;
import com.dinnervery.dto.auth.response.UnifiedLoginResponse;
import com.dinnervery.dto.customer.response.CustomerResponse;
import com.dinnervery.security.SecurityUtils;
import com.dinnervery.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/customer/signup")
    public ResponseEntity<CustomerResponse> customerSignup(@RequestBody SignupRequest request) {
        CustomerResponse response = authService.customerSignup(request);
        return ResponseEntity.ok(response);
    }

    /**
     * 통합 로그인 API - 고객/직원 자동 구분
     * 응답의 role 필드로 구분: "CUSTOMER", "COOK", "DELIVERY"
     */
    @PostMapping("/login")
    public ResponseEntity<UnifiedLoginResponse> unifiedLogin(@RequestBody LoginRequest request) {
        UnifiedLoginResponse response = authService.unifiedLogin(request);
        return ResponseEntity.ok(response);
    }

    /**
     * 고객 정보 조회 (등급, 주문횟수)
     * 인증 필요
     */
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<CustomerResponse> getCustomerInfo(@PathVariable Long customerId) {
        SecurityUtils.validateCustomerAccess(customerId);
        CustomerResponse response = authService.getCustomerInfo(customerId);
        return ResponseEntity.ok(response);
    }

}
