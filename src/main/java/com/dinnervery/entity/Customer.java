package com.dinnervery.entity;

import com.dinnervery.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

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

    @Column(name = "vip_start_date")
    private LocalDate vipStartDate;

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
        checkMonthlyReset();
        
        if (this.orderCount >= 15) {
            this.grade = CustomerGrade.VIP;
            if (this.vipStartDate == null) {
                this.vipStartDate = LocalDate.now();
            }
        } else {
            this.grade = CustomerGrade.BASIC;
            this.vipStartDate = null;
        }
    }

    private void checkMonthlyReset() {
        if (this.vipStartDate != null) {
            LocalDate oneMonthLater = this.vipStartDate.plusMonths(1);
            if (LocalDate.now().isAfter(oneMonthLater)) {
                this.orderCount = 0;
                this.grade = CustomerGrade.BASIC;
                this.vipStartDate = null;
            }
        }
    }

    public enum CustomerGrade {
        BASIC, VIP
    }
}



