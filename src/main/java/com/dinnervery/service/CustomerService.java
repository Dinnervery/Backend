package com.dinnervery.service;

import com.dinnervery.dto.customer.response.CustomerResponse;
import com.dinnervery.entity.Customer;
import com.dinnervery.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomerService {

    private final CustomerRepository customerRepository;

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

