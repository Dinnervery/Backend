package com.dinnervery.service;

import com.dinnervery.dto.auth.request.SignupRequest;
import com.dinnervery.dto.auth.request.LoginRequest;
import com.dinnervery.dto.auth.response.UnifiedLoginResponse;
import com.dinnervery.dto.customer.response.CustomerResponse;
import com.dinnervery.entity.Customer;
import com.dinnervery.entity.Staff;
import com.dinnervery.repository.CustomerRepository;
import com.dinnervery.repository.StaffRepository;
import com.dinnervery.security.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final CustomerRepository customerRepository;
    private final StaffRepository staffRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    @Transactional
    public CustomerResponse customerSignup(SignupRequest request) {
        if (customerRepository.existsByLoginId(request.getLoginId())) {
            throw new IllegalStateException("이미 존재하는 계정입니다.");
        }

        Customer customer = Customer.builder()
                .loginId(request.getLoginId())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .phoneNumber(request.getPhoneNumber())
                .address(request.getAddress())
                .build();

        Customer savedCustomer = customerRepository.save(customer);

        return new CustomerResponse(
                savedCustomer.getId(),
                savedCustomer.getLoginId(),
                savedCustomer.getName(),
                savedCustomer.getPhoneNumber(),
                savedCustomer.getGrade().toString(),
                savedCustomer.getOrderCount()
        );
    }

    /**
     * 통합 로그인 - 고객/직원 자동 구분
     * 고객 테이블에서 먼저 찾고, 없으면 직원 테이블에서 찾음
     */
    public UnifiedLoginResponse unifiedLogin(LoginRequest request) {
        // 고객 테이블에서 먼저 찾기
        Customer customer = customerRepository.findByLoginId(request.getLoginId()).orElse(null);
        
        if (customer != null) {
            // 고객인 경우
            if (!passwordEncoder.matches(request.getPassword(), customer.getPassword())) {
                throw new IllegalArgumentException("로그인에 실패했습니다");
            }

            String token = jwtProvider.generateToken(customer.getId(), customer.getLoginId(), "CUSTOMER");

            return new UnifiedLoginResponse(
                    customer.getId(),
                    customer.getLoginId(),
                    customer.getName(),
                    "CUSTOMER",
                    token,
                    customer.getGrade().toString(),
                    null  // task는 null
            );
        }

        // 직원 테이블에서 찾기
        Staff staff = staffRepository.findByLoginId(request.getLoginId())
                .orElseThrow(() -> new IllegalArgumentException("로그인에 실패했습니다"));

        if (!passwordEncoder.matches(request.getPassword(), staff.getPassword())) {
            throw new IllegalArgumentException("로그인에 실패했습니다");
        }

        String role = staff.getTask() == Staff.StaffTask.COOK ? "COOK" : "DELIVERY";
        String token = jwtProvider.generateToken(staff.getId(), staff.getLoginId(), role);

        return new UnifiedLoginResponse(
                staff.getId(),
                staff.getLoginId(),
                staff.getName(),
                role,
                token,
                null,  // grade는 null
                staff.getTask().toString()
        );
    }

    public CustomerResponse getCustomerInfo(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("고객을 찾을 수 없습니다: " + customerId));

        return new CustomerResponse(
                customer.getId(),
                customer.getLoginId(),
                customer.getName(),
                customer.getPhoneNumber(),
                customer.getGrade().toString(),
                customer.getOrderCount()
        );
    }
}

