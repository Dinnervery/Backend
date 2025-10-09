package com.dinnervery.service;

import com.dinnervery.entity.Menu;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class DiscountPolicy {

    @Value("${business.menu.special-dishes.champagne-festival-dinner.name}")
    private String champagneFestivalDinnerName;
    
    @Value("${business.menu.special-dishes.champagne-festival-dinner.price}")
    private int champagneFestivalDinnerPrice;

    /**
     * 특별 메뉴의 가격을 반환
     * @param menu 메뉴
     * @return 특별 메뉴면 특별 가격, 아니면 기본 가격
     */
    public int getMenuPrice(Menu menu) {
        if (champagneFestivalDinnerName.equals(menu.getName())) {
            return champagneFestivalDinnerPrice;
        }
        return menu.getPrice();
    }
}
