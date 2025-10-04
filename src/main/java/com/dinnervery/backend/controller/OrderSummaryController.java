package com.dinnervery.backend.controller;

import com.dinnervery.backend.dto.request.OrderSummaryRequest;
import com.dinnervery.backend.entity.Menu;
import com.dinnervery.backend.entity.MenuOption;
import com.dinnervery.backend.entity.ServingStyle;
import com.dinnervery.backend.repository.MenuOptionRepository;
import com.dinnervery.backend.repository.MenuRepository;
import com.dinnervery.backend.repository.ServingStyleRepository;
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
    private final ServingStyleRepository servingStyleRepository;

    // 주문 내역 확인을 위한 주문 요약 조회
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
                    optionMap.put("name", option.getItemName());
                    optionMap.put("quantity", selectedOption.getQuantity());
                    optionMap.put("unitPrice", option.getItemPrice());
                    optionMap.put("total", option.getItemPrice() * selectedOption.getQuantity());
                    return optionMap;
                })
                .collect(Collectors.toList());
        
        // 메뉴에 옵션 추가
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
        
        // 선택된 서빙 스타일 조회 (필수)
        ServingStyle servingStyle = servingStyleRepository.findById(request.getServingStyleId())
                .orElseThrow(() -> new IllegalArgumentException("서빙 스타일을 찾을 수 없습니다: " + request.getServingStyleId()));
        
        // Style 섹션
        Map<String, Object> servingStyleData = new HashMap<>();
        servingStyleData.put("styleId", servingStyle.getId());
        servingStyleData.put("name", servingStyle.getName());
        servingStyleData.put("quantity", 1);
        servingStyleData.put("unitPrice", servingStyle.getExtraPrice());
        servingStyleData.put("total", servingStyle.getExtraPrice());
        response.put("servingStyle", servingStyleData);
        
        // 총 가격 계산
        int totalPrice = menu.getPrice() + 
                options.stream().mapToInt(option -> (Integer) option.get("total")).sum() + 
                servingStyle.getExtraPrice();
        
        response.put("totalPrice", totalPrice);
        
        return ResponseEntity.ok(response);
    }
}
