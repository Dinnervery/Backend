package com.dinnervery.controller;

import com.dinnervery.repository.CustomerRepository;
import com.dinnervery.repository.EmployeeRepository;
import com.dinnervery.dto.member.SignupRequest;
import com.dinnervery.dto.member.LoginRequest;
import com.dinnervery.entity.Customer;
import com.dinnervery.entity.Employee;
import com.dinnervery.service.BusinessHoursService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MemberController {

    private final CustomerRepository customerRepository;
    private final EmployeeRepository employeeRepository;
    private final BusinessHoursService businessHoursService;

    // 영업시간 조회
    @GetMapping("/business-hours/status")
    public ResponseEntity<Map<String, Object>> getBusinessHoursStatus() {
        BusinessHoursService.BusinessHoursStatus status = businessHoursService.getBusinessHoursStatus();
        
        Map<String, Object> response = new HashMap<>();
        response.put("isOpen", status.isOpen());
        response.put("openTime", status.getOpenTime().toString());
        response.put("closeTime", status.getCloseTime().toString());
        response.put("lastOrderTime", status.getLastOrderTime().toString());
        response.put("message", status.getMessage());
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/auth/customer/signup")
    public ResponseEntity<Map<String, Object>> customerSignup(@RequestBody SignupRequest request) {
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

        Map<String, Object> response = new HashMap<>();
        response.put("customerId", savedCustomer.getId());
        response.put("loginId", savedCustomer.getLoginId());
        response.put("name", savedCustomer.getName());
        response.put("phoneNumber", savedCustomer.getPhoneNumber());
        response.put("grade", savedCustomer.getGrade());

        return ResponseEntity.ok(response);
    }


    @PostMapping("/auth/customer/login")
    public ResponseEntity<Map<String, Object>> customerLogin(@RequestBody LoginRequest request) {
        Customer customer = customerRepository.findByLoginId(request.getLoginId())
                .orElseThrow(() -> new IllegalArgumentException("로그인에 실패했습니다"));

        if (!customer.getPassword().equals(request.getPassword())) {
            throw new IllegalArgumentException("로그인에 실패했습니다");
        }

        Map<String, Object> response = new HashMap<>();
        response.put("customerId", customer.getId());
        response.put("loginId", customer.getLoginId());
        response.put("name", customer.getName());
        response.put("grade", customer.getGrade());
        response.put("token", "eyJhbGciOiJIUzI1NiIs..."); // 실제로는 JWT 토큰 생성

        return ResponseEntity.ok(response);
    }

    @PostMapping("/auth/employee/login")
    public ResponseEntity<Map<String, Object>> employeeLogin(@RequestBody LoginRequest request) {
        Employee employee = employeeRepository.findByLoginId(request.getLoginId())
                .orElseThrow(() -> new IllegalArgumentException("로그인에 실패했습니다"));

        if (!employee.getPassword().equals(request.getPassword())) {
            throw new IllegalArgumentException("로그인에 실패했습니다");
        }

        Map<String, Object> response = new HashMap<>();
        response.put("employeeId", employee.getId());
        response.put("loginId", employee.getLoginId());
        response.put("name", employee.getName());
        response.put("task", employee.getTask());
        response.put("token", "eyJhbGciOiJIUzI1NiIs..."); // 실제로는 JWT 토큰 생성

        return ResponseEntity.ok(response);
    }

    @GetMapping("/members/{id}")
    public ResponseEntity<Map<String, Object>> getCustomer(@PathVariable Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("고객을 찾을 수 없습니다: " + id));

        Map<String, Object> response = new HashMap<>();
        
        response.put("customerId", customer.getId());
        response.put("loginId", customer.getLoginId());
        response.put("name", customer.getName());
        response.put("phoneNumber", customer.getPhoneNumber());
        response.put("grade", customer.getGrade());
        response.put("orderCount", customer.getOrderCount());

        return ResponseEntity.ok(response);
    }
}
