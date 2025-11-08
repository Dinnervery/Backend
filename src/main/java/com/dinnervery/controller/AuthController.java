package com.dinnervery.controller;

import com.dinnervery.dto.auth.request.SignupRequest;
import com.dinnervery.dto.auth.request.LoginRequest;
import com.dinnervery.dto.auth.response.LoginResponse;
import com.dinnervery.dto.auth.response.StaffAuthResponse;
import com.dinnervery.dto.customer.response.CustomerResponse;
import com.dinnervery.entity.Customer;
import com.dinnervery.entity.Staff;
import com.dinnervery.repository.CustomerRepository;
import com.dinnervery.repository.StaffRepository;
import com.dinnervery.security.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final CustomerRepository customerRepository;
    private final StaffRepository staffRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    @PostMapping("/customer/signup")
    public ResponseEntity<CustomerResponse> customerSignup(@RequestBody SignupRequest request) {
        // 중복 검증
        if (customerRepository.existsByLoginId(request.getLoginId())) {
            throw new IllegalStateException("이미 존재하는 계정입니다.");
        }

        // 고객 생성 (비밀번호 암호화)
        Customer customer = Customer.builder()
                .loginId(request.getLoginId())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .phoneNumber(request.getPhoneNumber())
                .address(request.getAddress())
                .build();

        Customer savedCustomer = customerRepository.save(customer);

        CustomerResponse response = new CustomerResponse(
                savedCustomer.getId(),
                savedCustomer.getLoginId(),
                savedCustomer.getName(),
                savedCustomer.getPhoneNumber(),
                savedCustomer.getGrade().toString(),
                savedCustomer.getOrderCount()
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/customer/login")
    public ResponseEntity<LoginResponse> customerLogin(@RequestBody LoginRequest request) {
        Customer customer = customerRepository.findByLoginId(request.getLoginId())
                .orElseThrow(() -> new IllegalArgumentException("로그인에 실패했습니다"));

        if (!passwordEncoder.matches(request.getPassword(), customer.getPassword())) {
            throw new IllegalArgumentException("로그인에 실패했습니다");
        }

        // JWT 토큰 생성
        String token = jwtProvider.generateToken(customer.getId(), customer.getLoginId(), "CUSTOMER");

        LoginResponse response = new LoginResponse(
                customer.getId(),
                customer.getLoginId(),
                customer.getName(),
                customer.getGrade().toString(),
                token
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/staff/login")
    public ResponseEntity<StaffAuthResponse> staffLogin(@RequestBody LoginRequest request) {
        Staff staff = staffRepository.findByLoginId(request.getLoginId())
                .orElseThrow(() -> new IllegalArgumentException("로그인에 실패했습니다"));

        if (!passwordEncoder.matches(request.getPassword(), staff.getPassword())) {
            throw new IllegalArgumentException("로그인에 실패했습니다");
        }

        // Staff의 task에 따라 역할 설정 (COOK 또는 DELIVERY)
        String role = staff.getTask() == Staff.StaffTask.COOK ? "COOK" : "DELIVERY";
        
        // JWT 토큰 생성
        String token = jwtProvider.generateToken(staff.getId(), staff.getLoginId(), role);

        StaffAuthResponse response = new StaffAuthResponse(
                staff.getId(),
                staff.getLoginId(),
                staff.getName(),
                staff.getTask().toString(),
                token
        );

        return ResponseEntity.ok(response);
    }

}
