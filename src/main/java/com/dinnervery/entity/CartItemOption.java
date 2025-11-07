package com.dinnervery.entity;

import com.dinnervery.common.BaseEntity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "cart_item_options")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CartItemOption extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "cart_item_id", nullable = false)
	private CartItem cartItem;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "menu_option_id", nullable = false)
	private MenuOption menuOption;

	@Column(name = "quantity", nullable = false)
	private Integer quantity;

	@Builder
	public CartItemOption(MenuOption menuOption, Integer quantity) {
		if (quantity != null && quantity < 1) {
			throw new IllegalArgumentException("수량은 1 이상이어야 합니다.");
		}
		this.menuOption = menuOption;
		this.quantity = quantity;
	}

	public void setCartItem(CartItem cartItem) {
		this.cartItem = cartItem;
		if (cartItem != null && !cartItem.getCartItemOptions().contains(this)) {
			cartItem.addCartItemOption(this);
		}
	}

	public void updateQuantity(Integer newQuantity) {
		if (newQuantity == null || newQuantity < 1) {
			throw new IllegalArgumentException("수량은 1 이상이어야 합니다.");
		}
		this.quantity = newQuantity;
		if (this.cartItem != null) {
			this.cartItem.calculateItemPrice();
		}
	}

	public int calculateExtraCost() {
		return menuOption.calculateExtraCost(quantity);
	}
}


