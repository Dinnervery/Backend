package com.dinnervery.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dinnervery.entity.Address;

import java.util.List;

public interface AddressRepository extends JpaRepository<Address, Long> {
    List<Address> findByCustomer_Id(Long customerId);
}


