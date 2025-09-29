package com.dinnervery.backend.service;

import com.dinnervery.backend.dto.order.OrderCreateRequest;
import com.dinnervery.backend.dto.order.OrderItemRequest;
import com.dinnervery.backend.dto.order.OrderItemOptionRequest;
import com.dinnervery.backend.entity.Customer;
import com.dinnervery.backend.entity.Menu;
import com.dinnervery.backend.entity.MenuOption;
import com.dinnervery.backend.entity.ServingStyle;
import com.dinnervery.backend.repository.CustomerRepository;
import com.dinnervery.backend.repository.MenuRepository;
import com.dinnervery.backend.repository.MenuOptionRepository;
import com.dinnervery.backend.repository.ServingStyleRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
@RequiredArgsConstructor
public class PriceCalculator {

    private final MenuRepository menuRepository;
    private final MenuOptionRepository menuOptionRepository;
    private final ServingStyleRepository servingStyleRepository;
    private final CustomerRepository customerRepository;

    public int calcOrderTotal(OrderCreateRequest req) {
        BigDecimal total = BigDecimal.ZERO;

        for (OrderItemRequest itemRequest : req.getItems()) {
            // 메뉴 조회
            Menu menu = menuRepository.findById(itemRequest.getMenuId())
                    .orElseThrow(() -> new IllegalArgumentException("메뉴를 찾을 수 없습니다: " + itemRequest.getMenuId()));

            // 서빙 스타일 조회
            ServingStyle servingStyle = servingStyleRepository.findById(itemRequest.getServingStyleId())
                    .orElseThrow(() -> new IllegalArgumentException("서빙 스타일을 찾을 수 없습니다: " + itemRequest.getServingStyleId()));

            // 기본 가격 계산
            BigDecimal base = menu.getPrice();
            
            // 샴페인 축제 디너 특별 케이스
            if ("샴페인 축제 디너".equals(menu.getName())) {
                base = new BigDecimal("90000");
            }

            // 서빙 스타일 추가비
            BigDecimal styleExtra = servingStyle.getExtraPrice();

            // 옵션 델타 계산
            BigDecimal optionDelta = BigDecimal.ZERO;
            if (itemRequest.getOptions() != null) {
                for (OrderItemOptionRequest optionRequest : itemRequest.getOptions()) {
                    MenuOption menuOption = menuOptionRepository.findById(optionRequest.getMenuOptionId())
                            .orElseThrow(() -> new IllegalArgumentException("메뉴 옵션을 찾을 수 없습니다: " + optionRequest.getMenuOptionId()));
                    
                    // MenuOption의 calculateExtraCost 메서드 사용
                    int extraCost = menuOption.calculateExtraCost(optionRequest.getOrderedQty());
                    optionDelta = optionDelta.add(new BigDecimal(extraCost));
                }
            }

            // 아이템 소계 계산
            BigDecimal itemSubtotal = base.add(styleExtra).add(optionDelta)
                    .multiply(new BigDecimal(itemRequest.getOrderedQty()));

            total = total.add(itemSubtotal);
        }

        // VIP 할인 적용
        Customer customer = customerRepository.findById(req.getCustomerId())
                .orElseThrow(() -> new IllegalArgumentException("고객을 찾을 수 없습니다: " + req.getCustomerId()));

        if (customer.getOrderCount() >= 10 && (customer.getOrderCount() + 1) % 11 == 0) {
            total = total.multiply(new BigDecimal("0.9")).setScale(0, RoundingMode.DOWN);
        }

        return total.intValue();
    }
}
