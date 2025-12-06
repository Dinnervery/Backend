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

	@Column(name = "option_id", nullable = false)
	private Long optionId;

	@Column(name = "option_name", nullable = false)
	private String optionName;

	@Column(name = "option_price", nullable = false)
	private int optionPrice;

	@Column(name = "default_qty", nullable = false)
	private int defaultQty;

	@Column(name = "quantity", nullable = false)
	private Integer quantity;

	@Column(name = "storage_consumption", nullable = false)
	private int storageConsumption = 1;

	@Builder
	public CartItemOption(Long optionId, String optionName, int optionPrice, int defaultQty, Integer quantity, Integer storageConsumption) {
		if (quantity != null && quantity < 1) {
			throw new IllegalArgumentException("수량은 1 이상이어야 합니다.");
		}
		this.optionId = optionId;
		this.optionName = optionName;
		this.optionPrice = optionPrice;
		this.defaultQty = defaultQty;
		this.quantity = quantity;
		this.storageConsumption = storageConsumption != null ? storageConsumption : 1;
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
		int difference = quantity - defaultQty;
		if (difference > 0) {
			return difference * optionPrice;
		} else {
			return 0;
		}
	}
}


