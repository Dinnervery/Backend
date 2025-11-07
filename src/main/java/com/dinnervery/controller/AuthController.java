package com.dinnervery.controller;

import com.dinnervery.dto.auth.request.SignupRequest;
import com.dinnervery.dto.auth.request.LoginRequest;
import com.dinnervery.dto.auth.response.AuthResponse;
import com.dinnervery.dto.auth.response.LoginResponse;
import com.dinnervery.dto.auth.response.StaffAuthResponse;
import com.dinnervery.dto.customer.response.CustomerResponse;
import com.dinnervery.entity.Customer;
import com.dinnervery.entity.Staff;
import com.dinnervery.repository.CustomerRepository;
import com.dinnervery.repository.StaffRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final CustomerRepository customerRepository;
    private final StaffRepository staffRepository;

    @PostMapping("/customer/signup")
    public ResponseEntity<AuthResponse> customerSignup(@RequestBody SignupRequest request) {
        // 중복 검증
        if (customerRepository.existsByLoginId(request.getLoginId())) {
            throw new IllegalStateException("이미 존재하는 계정입니다.");
        }

        // 고객 생성
        Customer customer = Customer.builder()
                .loginId(request.getLoginId())
                .password(request.getPassword())
                .name(request.getName())
                .phoneNumber(request.getPhoneNumber())
                .address(request.getAddress())
                .build();

        Customer savedCustomer = customerRepository.save(customer);

        AuthResponse response = new AuthResponse(
                savedCustomer.getId(),
                savedCustomer.getLoginId(),
                savedCustomer.getName(),
                savedCustomer.getPhoneNumber(),
                savedCustomer.getAddress(),
                savedCustomer.getGrade().toString(),
                "eyJhbGciOiJIUzI1NiIs..." // 실제로는 JWT 토큰 생성
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/customer/login")
    public ResponseEntity<LoginResponse> customerLogin(@RequestBody LoginRequest request) {
        Customer customer = customerRepository.findByLoginId(request.getLoginId())
                .orElseThrow(() -> new IllegalArgumentException("로그인에 실패했습니다"));

        if (!customer.getPassword().equals(request.getPassword())) {
            throw new IllegalArgumentException("로그인에 실패했습니다");
        }

        LoginResponse response = new LoginResponse(
                customer.getId(),
                customer.getLoginId(),
                customer.getName(),
                customer.getGrade().toString(),
                "eyJhbGciOiJIUzI1NiIs..." // 실제로는 JWT 토큰 생성
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/staff/login")
    public ResponseEntity<StaffAuthResponse> staffLogin(@RequestBody LoginRequest request) {
        Staff staff = staffRepository.findByLoginId(request.getLoginId())
                .orElseThrow(() -> new IllegalArgumentException("로그인에 실패했습니다"));

        if (!staff.getPassword().equals(request.getPassword())) {
            throw new IllegalArgumentException("로그인에 실패했습니다");
        }

        StaffAuthResponse response = new StaffAuthResponse(
                staff.getId(),
                staff.getLoginId(),
                staff.getName(),
                staff.getTask().toString(),
                "eyJhbGciOiJIUzI1NiIs..." // 실제로는 JWT 토큰 생성
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/customers/{id}")
    public ResponseEntity<CustomerResponse> getCustomer(@PathVariable Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("고객을 찾을 수 없습니다: " + id));

        CustomerResponse response = new CustomerResponse(
                customer.getId(),
                customer.getLoginId(),
                customer.getName(),
                customer.getPhoneNumber(),
                customer.getGrade().toString(),
                customer.getOrderCount()
        );

        return ResponseEntity.ok(response);
    }
}
