package com.dinnervery.controller;

import com.dinnervery.dto.member.SignupRequest;
import com.dinnervery.dto.member.LoginRequest;
import com.dinnervery.dto.response.AuthResponse;
import com.dinnervery.dto.response.EmployeeAuthResponse;
import com.dinnervery.dto.response.CustomerResponse;
import com.dinnervery.entity.Customer;
import com.dinnervery.entity.Employee;
import com.dinnervery.repository.CustomerRepository;
import com.dinnervery.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final CustomerRepository customerRepository;
    private final EmployeeRepository employeeRepository;

    @PostMapping("/customer/signup")
    public ResponseEntity<AuthResponse> customerSignup(@RequestBody SignupRequest request) {
        // 중복 검증
        if (customerRepository.existsByLoginId(request.getLoginId())) {
            throw new IllegalArgumentException("이미 존재하는 로그인 ID입니다: " + request.getLoginId());
        }

        // 고객 생성
        Customer customer = Customer.builder()
                .loginId(request.getLoginId())
                .password(request.getPassword())
                .name(request.getName())
                .phoneNumber(request.getPhoneNumber())
                .build();

        Customer savedCustomer = customerRepository.save(customer);

        AuthResponse response = new AuthResponse(
                savedCustomer.getId(),
                savedCustomer.getLoginId(),
                savedCustomer.getName(),
                savedCustomer.getPhoneNumber(),
                savedCustomer.getGrade().toString(),
                "eyJhbGciOiJIUzI1NiIs..." // 실제로는 JWT 토큰 생성
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/customer/login")
    public ResponseEntity<AuthResponse> customerLogin(@RequestBody LoginRequest request) {
        Customer customer = customerRepository.findByLoginId(request.getLoginId())
                .orElseThrow(() -> new IllegalArgumentException("로그인에 실패했습니다"));

        if (!customer.getPassword().equals(request.getPassword())) {
            throw new IllegalArgumentException("로그인에 실패했습니다");
        }

        AuthResponse response = new AuthResponse(
                customer.getId(),
                customer.getLoginId(),
                customer.getName(),
                customer.getPhoneNumber(),
                customer.getGrade().toString(),
                "eyJhbGciOiJIUzI1NiIs..." // 실제로는 JWT 토큰 생성
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/employee/login")
    public ResponseEntity<EmployeeAuthResponse> employeeLogin(@RequestBody LoginRequest request) {
        Employee employee = employeeRepository.findByLoginId(request.getLoginId())
                .orElseThrow(() -> new IllegalArgumentException("로그인에 실패했습니다"));

        if (!employee.getPassword().equals(request.getPassword())) {
            throw new IllegalArgumentException("로그인에 실패했습니다");
        }

        EmployeeAuthResponse response = new EmployeeAuthResponse(
                employee.getId(),
                employee.getLoginId(),
                employee.getName(),
                employee.getTask().toString(),
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
