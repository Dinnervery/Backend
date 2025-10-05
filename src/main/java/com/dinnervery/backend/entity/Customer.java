package com.dinnervery.backend.entity;

import com.dinnervery.backend.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "customers")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Customer extends BaseEntity {

    @Column(name = "login_id", nullable = false, unique = true)
    private String loginId;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    @Column(name = "address", nullable = false)
    private String address;

    @Column(name = "order_count", nullable = false)
    private Integer orderCount = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "grade", nullable = false)
    private CustomerGrade grade = CustomerGrade.BASIC;

    @Builder
    public Customer(String loginId, String password, String name, String phoneNumber, String address) {
        this.loginId = loginId;
        this.password = password;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.address = address;
    }

    public void incrementOrderCount() {
        this.orderCount++;
        updateGradeByOrderCount();
    }

    public void updateGradeByOrderCount() {
        if (this.orderCount >= 15) {
            this.grade = CustomerGrade.VIP;
        } else {
            this.grade = CustomerGrade.BASIC;
        }
    }

    public boolean isVipDiscountEligible() {
        return this.grade == CustomerGrade.VIP && (this.orderCount + 1) % 16 == 0;
    }

    public enum CustomerGrade {
        BASIC, VIP
    }
}



