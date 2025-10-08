package com.dinnervery.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dinnervery.entity.Cart;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByCustomer_Id(Long customerId);
}


