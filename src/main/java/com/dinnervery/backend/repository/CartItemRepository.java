package com.dinnervery.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dinnervery.backend.entity.CartItem;

import java.util.List;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByCart_Id(Long cartId);
    List<CartItem> findByCart_IdAndMenu_Id(Long cartId, Long menuId);
}


