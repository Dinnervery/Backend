package com.dinnervery.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dinnervery.entity.CartItemOption;

import java.util.Optional;

public interface CartItemOptionRepository extends JpaRepository<CartItemOption, Long> {
	Optional<CartItemOption> findByCartItem_IdAndMenuOption_Id(Long cartItemId, Long menuOptionId);
}


