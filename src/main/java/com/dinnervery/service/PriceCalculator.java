package com.dinnervery.service;

import com.dinnervery.dto.request.OrderCreateRequest;
import com.dinnervery.dto.request.OrderItemCreateRequest;
import com.dinnervery.entity.Customer;
import com.dinnervery.entity.Menu;
import com.dinnervery.entity.ServingStyle;
import com.dinnervery.repository.CustomerRepository;
import com.dinnervery.repository.MenuRepository;
import com.dinnervery.repository.ServingStyleRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PriceCalculator {

    private final MenuRepository menuRepository;
    private final ServingStyleRepository servingStyleRepository;
    private final CustomerRepository customerRepository;
    private final DiscountPolicy discountPolicy;

    @Value("${business.customer.vip-discount-rate}")
    private int vipDiscountRate;

    public int calcOrderTotal(OrderCreateRequest req) {
        int total = 0;

        for (OrderItemCreateRequest itemRequest : req.getOrderItems()) {
            // 메뉴 조회
            Menu menu = menuRepository.findById(itemRequest.getMenuId())
                    .orElseThrow(() -> new IllegalArgumentException("메뉴를 찾을 수 없습니다: " + itemRequest.getMenuId()));

            // 서빙 스타일 조회
            ServingStyle servingStyle = servingStyleRepository.findById(itemRequest.getServingStyleId())
                    .orElseThrow(() -> new IllegalArgumentException("서빙 스타일을 찾을 수 없습니다: " + itemRequest.getServingStyleId()));

            // 할인 정책을 통한 가격 계산
            int base = discountPolicy.getMenuPrice(menu);

            // 서빙 스타일 추가 가격
            int styleExtra = servingStyle.getExtraPrice();

            // 아이템별 계산 계산 (옵션은 현재 OrderItemCreateRequest에서 지원하지 않음)
            int itemSubtotal = (base + styleExtra) * itemRequest.getQuantity();

            total += itemSubtotal;
        }

        // VIP 고객 할인
        Customer customer = customerRepository.findById(req.getCustomerId())
                .orElseThrow(() -> new IllegalArgumentException("고객을 찾을 수 없습니다: " + req.getCustomerId()));

        if (customer.isVipDiscountEligible()) {
            total = (int) (total * (1 - vipDiscountRate / 100.0));
        }

        return total;
    }
}
