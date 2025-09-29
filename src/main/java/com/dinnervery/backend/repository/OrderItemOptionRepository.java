package com.dinnervery.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dinnervery.backend.entity.OrderItemOption;

public interface OrderItemOptionRepository extends JpaRepository<OrderItemOption, Long> {
}


