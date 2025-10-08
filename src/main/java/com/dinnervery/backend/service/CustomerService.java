package com.dinnervery.backend.service;

import com.dinnervery.backend.dto.CustomerDto;
import com.dinnervery.backend.dto.request.CustomerCreateRequest;
import com.dinnervery.backend.entity.Customer;
import com.dinnervery.backend.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomerService {

    private final CustomerRepository customerRepository;

    @Transactional
    public CustomerDto createCustomer(CustomerCreateRequest request) {
        // 중복 검증
        if (customerRepository.existsByLoginId(request.getLoginId())) {
            throw new IllegalArgumentException("이미 존재하는 로그인 ID입니다: " + request.getLoginId());
        }

        Customer customer = Customer.builder()
                .loginId(request.getLoginId())
                .password(request.getPassword())
                .name(request.getName())
                .phoneNumber(request.getPhone())
                .build();

        Customer savedCustomer = customerRepository.save(customer);
        return CustomerDto.from(savedCustomer);
    }
}


