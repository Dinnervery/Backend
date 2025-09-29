package com.dinnervery.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dinnervery.backend.entity.Address;

import java.util.List;

public interface AddressRepository extends JpaRepository<Address, Long> {
    List<Address> findByCustomer_Id(Long customerId);
}


