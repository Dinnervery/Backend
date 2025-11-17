package com.dinnervery.controller;

import com.dinnervery.dto.auth.request.SignupRequest;
import com.dinnervery.dto.auth.request.LoginRequest;
import com.dinnervery.dto.auth.response.LoginResponse;
import com.dinnervery.dto.auth.response.StaffAuthResponse;
import com.dinnervery.dto.customer.response.CustomerResponse;
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

    @PostMapping("/customer/login")
    public ResponseEntity<LoginResponse> customerLogin(@RequestBody LoginRequest request) {
        LoginResponse response = authService.customerLogin(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/staff/login")
    public ResponseEntity<StaffAuthResponse> staffLogin(@RequestBody LoginRequest request) {
        StaffAuthResponse response = authService.staffLogin(request);
        return ResponseEntity.ok(response);
    }

}
