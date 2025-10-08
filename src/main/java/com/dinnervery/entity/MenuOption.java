package com.dinnervery.entity;

import com.dinnervery.common.BaseEntity;

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
    private int itemPrice; // option 추�? ?��? (???�위)
    
    @Column(name = "default_qty", nullable = false)
    @Builder.Default
    private int defaultQty = 1; // 기본 ?�량
    
    /**
     * 구성??추�?/?�거???�른 추�? 비용 계산
     * @param orderedQty 주문 ?�량
     * @return 추�? 비용 (?�수: 추�?, 0: 기본, ?�수: ?�거)
     */
    public int calculateExtraCost(int orderedQty) {
        int difference = orderedQty - defaultQty;
        if (difference > 0) {
            // 추�?: 추�????�량만큼 가�?추�?
            return difference * itemPrice;
        } else {
            // ?�거: 감액 ?�음 (0??
            return 0;
        }
    }
}
