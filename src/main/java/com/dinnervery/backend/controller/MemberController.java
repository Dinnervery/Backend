package com.dinnervery.backend.controller;

import com.dinnervery.backend.repository.CustomerRepository;
import com.dinnervery.backend.dto.member.SignupRequest;
import com.dinnervery.backend.entity.Customer;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MemberController {

    private final CustomerRepository customerRepository;

    @PostMapping("/auth/signup")
    public ResponseEntity<Map<String, Object>> signup(@RequestBody SignupRequest request) {
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
        response.put("message", "회원가입이 완료되었습니다");

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
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
        response.put("orderCount", customer.getOrderCount());
        response.put("grade", customer.getGrade());

        return ResponseEntity.ok(response);
    }
}
