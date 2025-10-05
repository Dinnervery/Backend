package com.dinnervery.backend.service;

import com.dinnervery.backend.dto.request.OrderCreateRequest;
import com.dinnervery.backend.dto.request.OrderItemCreateRequest;
import com.dinnervery.backend.entity.Customer;
import com.dinnervery.backend.entity.Menu;
import com.dinnervery.backend.entity.ServingStyle;
import com.dinnervery.backend.repository.CustomerRepository;
import com.dinnervery.backend.repository.MenuRepository;
import com.dinnervery.backend.repository.ServingStyleRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PriceCalculator {

    private final MenuRepository menuRepository;
    private final ServingStyleRepository servingStyleRepository;
    private final CustomerRepository customerRepository;

    public int calcOrderTotal(OrderCreateRequest req) {
        int total = 0;

        for (OrderItemCreateRequest itemRequest : req.getOrderItems()) {
            // 메뉴 조회
            Menu menu = menuRepository.findById(itemRequest.getMenuId())
                    .orElseThrow(() -> new IllegalArgumentException("메뉴를 찾을 수 없습니다: " + itemRequest.getMenuId()));

            // 서빙 스타일 조회
            ServingStyle servingStyle = servingStyleRepository.findById(itemRequest.getServingStyleId())
                    .orElseThrow(() -> new IllegalArgumentException("서빙 스타일을 찾을 수 없습니다: " + itemRequest.getServingStyleId()));

            // 기본 가격 계산
            int base = menu.getPrice();
            
            // 샴페인 축제 디너 특별 케이스
            if ("샴페인 축제 디너".equals(menu.getName())) {
                base = 90000;
            }

            // 서빙 스타일 추가비
            int styleExtra = servingStyle.getExtraPrice();

            // 아이템 소계 계산 (옵션은 현재 OrderItemCreateRequest에서 지원하지 않음)
            int itemSubtotal = (base + styleExtra) * itemRequest.getQuantity();

            total += itemSubtotal;
        }

        // VIP 할인 적용
        Customer customer = customerRepository.findById(req.getCustomerId())
                .orElseThrow(() -> new IllegalArgumentException("고객을 찾을 수 없습니다: " + req.getCustomerId()));

        if (customer.isVipDiscountEligible()) {
            total = (int) (total * 0.9);
        }

        return total;
    }
}
