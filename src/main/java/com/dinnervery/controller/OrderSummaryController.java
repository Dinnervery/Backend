package com.dinnervery.controller;

import com.dinnervery.dto.order.request.OrderSummaryRequest;
import com.dinnervery.entity.Menu;
import com.dinnervery.entity.MenuOption;
import com.dinnervery.entity.Style;
import com.dinnervery.repository.MenuOptionRepository;
import com.dinnervery.repository.MenuRepository;
import com.dinnervery.repository.StyleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class OrderSummaryController {

    private final MenuRepository menuRepository;
    private final MenuOptionRepository menuOptionRepository;
    private final StyleRepository styleRepository;

    // 주문 요약 확인을 위한 주문 요약 조회
    @PostMapping("/order-summary")
    public ResponseEntity<Map<String, Object>> getOrderSummary(@RequestBody OrderSummaryRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        // 메뉴 정보 조회
        Menu menu = menuRepository.findById(request.getMenuId())
                .orElseThrow(() -> new IllegalArgumentException("메뉴를 찾을 수 없습니다: " + request.getMenuId()));
        
        // 선택된 구성품들 추가
        List<Map<String, Object>> options = request.getSelectedOptions().stream()
                .map(selectedOption -> {
                    MenuOption option = menuOptionRepository.findById(selectedOption.getOptionId())
                            .orElseThrow(() -> new IllegalArgumentException("옵션을 찾을 수 없습니다: " + selectedOption.getOptionId()));
                    
                    Map<String, Object> optionMap = new HashMap<>();
                    optionMap.put("optionId", option.getId());
                    optionMap.put("name", option.getName());
                    optionMap.put("quantity", selectedOption.getQuantity());
                    optionMap.put("unitPrice", option.getPrice());
                    optionMap.put("total", option.getPrice() * selectedOption.getQuantity());
                    return optionMap;
                })
                .collect(Collectors.toList());
        
        // 메뉴와 옵션 추가
        Map<String, Object> menuData = new HashMap<>();
        menuData.put("menuId", menu.getId());
        menuData.put("name", menu.getName());
        menuData.put("quantity", 1);
        menuData.put("unitPrice", menu.getPrice());
        menuData.put("total", menu.getPrice());
        menuData.put("options", options);
        
        List<Map<String, Object>> dinnerItems = new ArrayList<>();
        dinnerItems.add(menuData);
        response.put("dinnerItems", dinnerItems);
        
        // 선택된 스타일 조회
        Style style = styleRepository.findById(request.getStyleId())
                .orElseThrow(() -> new IllegalArgumentException("스타일을 찾을 수 없습니다: " + request.getStyleId()));
        
        // Style ?�션
        Map<String, Object> styleData = new HashMap<>();
        styleData.put("styleId", style.getId());
        styleData.put("name", style.getName());
        styleData.put("quantity", 1);
        styleData.put("unitPrice", style.getExtraPrice());
        styleData.put("total", style.getExtraPrice());
        response.put("style", styleData);
        
        // 총가격계산
        int totalPrice = menu.getPrice() + 
                options.stream().mapToInt(option -> (Integer) option.get("total")).sum() + 
                style.getExtraPrice();
        
        response.put("totalPrice", totalPrice);
        
        return ResponseEntity.ok(response);
    }
}
