package com.dinnervery.backend.entity;

import com.dinnervery.backend.common.BaseEntity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "addresses")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Address extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(name = "addr_detail", nullable = false)
    private String addrDetail;

    @Builder
    public Address(Customer customer, String addrDetail) {
        this.customer = customer;
        this.addrDetail = addrDetail;
    }

    public void updateAddressDetail(String newAddrDetail) {
        this.addrDetail = newAddrDetail;
    }
}
