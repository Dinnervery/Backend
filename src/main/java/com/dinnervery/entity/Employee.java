package com.dinnervery.entity;

import com.dinnervery.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "employees")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Employee extends BaseEntity {

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
    private EmployeeTask task;

    @Enumerated(EnumType.STRING)
    @Column(name = "work_status", nullable = false)
    private WorkStatus workStatus = WorkStatus.AVAILABLE;

    @Builder
    public Employee(String loginId, String password, String name, String phoneNumber, EmployeeTask task) {
        this.loginId = loginId;
        this.password = password;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.task = task;
    }

    public boolean hasCookPermission() {
        return this.task == EmployeeTask.COOK;
    }

    public boolean hasDeliveryPermission() {
        return this.task == EmployeeTask.DELIVERY;
    }

    public void startWork() {
        this.workStatus = WorkStatus.BUSY;
    }

    public void finishWork() {
        this.workStatus = WorkStatus.AVAILABLE;
    }

    public boolean isAvailable() {
        return this.workStatus == WorkStatus.AVAILABLE;
    }

    public enum EmployeeTask {
        COOK, DELIVERY
    }

    public enum WorkStatus {
        AVAILABLE, BUSY
    }
}
