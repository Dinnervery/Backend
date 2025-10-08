package com.dinnervery.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dinnervery.entity.OrderItemOption;

public interface OrderItemOptionRepository extends JpaRepository<OrderItemOption, Long> {
}


