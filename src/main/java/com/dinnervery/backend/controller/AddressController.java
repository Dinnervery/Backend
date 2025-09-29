package com.dinnervery.backend.controller;

import com.dinnervery.backend.dto.address.AddressCreateRequest;
import com.dinnervery.backend.entity.Address;
import com.dinnervery.backend.entity.Customer;
import com.dinnervery.backend.repository.AddressRepository;
import com.dinnervery.backend.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AddressController {

    private final AddressRepository addressRepository;
    private final CustomerRepository customerRepository;

    @PostMapping("/customers/{id}/addresses")
    public ResponseEntity<Map<String, Object>> createAddress(@PathVariable Long id, @RequestBody AddressCreateRequest request) {
        // 고객 조회
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("고객을 찾을 수 없습니다: " + id));

        // 주소 생성
        Address address = Address.builder()
                .customer(customer)
                .addrDetail(request.getAddrDetail())
                .build();

        Address savedAddress = addressRepository.save(address);

        Map<String, Object> response = new HashMap<>();
        response.put("addressId", savedAddress.getId());
        response.put("message", "주소가 생성되었습니다");

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/customers/{id}/addresses")
    public ResponseEntity<List<Map<String, Object>>> getAddressesByCustomer(@PathVariable Long id) {
        List<Address> addresses = addressRepository.findByCustomer_Id(id);

        List<Map<String, Object>> response = addresses.stream()
                .map(address -> {
                    Map<String, Object> addressMap = new HashMap<>();
                    addressMap.put("addressId", address.getId());
                    addressMap.put("addrDetail", address.getAddrDetail());
                    addressMap.put("createdAt", address.getCreatedAt());
                    return addressMap;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }
}
