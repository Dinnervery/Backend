package com.dinnervery.entity;

import com.dinnervery.common.BaseEntity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "storages")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Storage extends BaseEntity {

	@Column(name = "name", nullable = false, unique = true)
	private String name;

	@Column(name = "quantity", nullable = false)
	private int quantity;

	@Builder
	public Storage(String name, int quantity) {
		this.name = name;
		this.quantity = quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}
}


