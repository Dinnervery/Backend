package com.dinnervery.entity;

import com.dinnervery.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "staffs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Staff extends BaseEntity {

	@Column(name = "login_id", nullable = false, unique = true)
	private String loginId;

	@Column(name = "password", nullable = false)
	private String password;

	@Column(name = "name", nullable = false)
	private String name;

	@Column(name = "phone_number", nullable = false)
	private String phoneNumber;

	@Enumerated(EnumType.STRING)
	@Column(name = "task", nullable = false)
	private StaffTask task;

	@Builder
	public Staff(String loginId, String password, String name, String phoneNumber, StaffTask task) {
		this.loginId = loginId;
		this.password = password;
		this.name = name;
		this.phoneNumber = phoneNumber;
		this.task = task;
	}

	public boolean hasCookPermission() {
		return this.task == StaffTask.COOK;
	}

	public boolean hasDeliveryPermission() {
		return this.task == StaffTask.DELIVERY;
	}

	public enum StaffTask {
		COOK, DELIVERY
	}
}



