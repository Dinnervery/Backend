package com.dinnervery.entity;

import com.dinnervery.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Address> addresses = new ArrayList<>();

    @Column(name = "order_count", nullable = false)
    private Integer orderCount = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "grade", nullable = false)
    private CustomerGrade grade = CustomerGrade.BASIC;

    @Column(name = "vip_start_date")
    private LocalDate vipStartDate;

    @Builder
    public Customer(String loginId, String password, String name, String phoneNumber) {
        this.loginId = loginId;
        this.password = password;
        this.name = name;
        this.phoneNumber = phoneNumber;
    }

    public void incrementOrderCount() {
        this.orderCount++;
        updateGradeByOrderCount();
    }

    public void updateGradeByOrderCount() {
        // 등급 초기화 체크
        checkMonthlyReset();
        
        if (this.orderCount >= 15) {
            this.grade = CustomerGrade.VIP;
            // VIP가 된 날짜 설정 (처음 설정되는 경우에만)
            if (this.vipStartDate == null) {
                this.vipStartDate = LocalDate.now();
            }
        } else {
            this.grade = CustomerGrade.BASIC;
            this.vipStartDate = null;
        }
    }

    private void checkMonthlyReset() {
        // VIP 시작일이 있고 한달이 지났다면 초기화
        if (this.vipStartDate != null) {
            LocalDate oneMonthLater = this.vipStartDate.plusMonths(1);
            if (LocalDate.now().isAfter(oneMonthLater)) {
                this.orderCount = 0;
                this.grade = CustomerGrade.BASIC;
                this.vipStartDate = null;
            }
        }
    }

    public boolean isVipDiscountEligible() {
        // 등급 초기화 체크
        checkMonthlyReset();
        
        // VIP 등급이면 즉시 10% 할인 적용
        return this.grade == CustomerGrade.VIP;
    }

    public enum CustomerGrade {
        BASIC, VIP
    }
}



