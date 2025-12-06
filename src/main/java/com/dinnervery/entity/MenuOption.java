package com.dinnervery.entity;

import com.dinnervery.common.BaseEntity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "menu_options")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuOption extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_id")
    private Menu menu;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "price", nullable = false)
    private int price;
    
    @Column(name = "default_qty", nullable = false)
    @Builder.Default
    private int defaultQty = 1;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "storage_item_id")
    private Storage storageItem;

    @Column(name = "storage_consumption", nullable = false)
    @Builder.Default
    private int storageConsumption = 1;
    
    public int calculateExtraCost(int orderedQty) {
        int difference = orderedQty - defaultQty;
        if (difference > 0) {
            return difference * price;
        } else {
            return 0;
        }
    }
    
    public void updatePrice(int newPrice) {
        this.price = newPrice;
    }
    
    public void updateDefaultQty(int newDefaultQty) {
        this.defaultQty = newDefaultQty;
    }

    public void setStorageItem(Storage storage) {
        this.storageItem = storage;
    }

    public void setStorageConsumption(int storageConsumption) {
        this.storageConsumption = storageConsumption;
    }
}
