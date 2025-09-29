package com.dinnervery.backend.entity;

import com.dinnervery.backend.common.BaseEntity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuOption extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_id")
    private Menu menu;

    @Column(name = "item_name", nullable = false)
    private String itemName;

    @Column(name = "item_price", nullable = false)
    private int itemPrice; // option 추가 단가 (원 단위)
    
    @Column(name = "default_qty", nullable = false)
    @Builder.Default
    private int defaultQty = 1; // 기본 수량
    
    /**
     * 구성품 추가/제거에 따른 추가 비용 계산
     * @param orderedQty 주문 수량
     * @return 추가 비용 (양수: 추가, 0: 기본, 음수: 제거)
     */
    public int calculateExtraCost(int orderedQty) {
        int difference = orderedQty - defaultQty;
        if (difference > 0) {
            // 추가: 추가된 수량만큼 가격 추가
            return difference * itemPrice;
        } else {
            // 제거: 감액 없음 (0원)
            return 0;
        }
    }
}
