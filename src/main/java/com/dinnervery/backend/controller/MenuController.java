package com.dinnervery.backend.controller;

import com.dinnervery.backend.entity.Customer;
import com.dinnervery.backend.entity.Menu;
import com.dinnervery.backend.entity.MenuOption;
import com.dinnervery.backend.entity.ServingStyle;
import com.dinnervery.backend.repository.CustomerRepository;
import com.dinnervery.backend.repository.MenuOptionRepository;
import com.dinnervery.backend.repository.MenuRepository;
import com.dinnervery.backend.repository.ServingStyleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MenuController {

    private final MenuRepository menuRepository;
    private final MenuOptionRepository menuOptionRepository;
    private final ServingStyleRepository servingStyleRepository;
    private final CustomerRepository customerRepository;

    // 메뉴 목록 조회
    @GetMapping("/menus")
    public ResponseEntity<Map<String, Object>> getAllMenus(@RequestParam(required = false) Long customerId) {
        List<Menu> menus = menuRepository.findAll();
        
        List<Map<String, Object>> menuList = menus.stream()
                .map(menu -> {
                    Map<String, Object> menuMap = new HashMap<>();
                    menuMap.put("menuId", menu.getId());
                    menuMap.put("name", menu.getName());
                    menuMap.put("price", menu.getPrice());
                    menuMap.put("description", menu.getDescription());
                    
                    // VIP 고객인 경우 할인가 표시
                    if (customerId != null) {
                        Customer customer = customerRepository.findById(customerId).orElse(null);
                        if (customer != null && customer.getGrade() == Customer.CustomerGrade.VIP && customer.isVipDiscountEligible()) {
                            int discountedPrice = (int) (menu.getPrice() * 0.9); // 10% 할인
                            menuMap.put("discountedPrice", discountedPrice);
                        }
                    }
                    
                    return menuMap;
                })
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("menus", menuList);
        
        return ResponseEntity.ok(response);
    }


    // 구성품 수량 변경을 위한 메뉴 옵션 조회
    @GetMapping("/menus/{menuId}/options/quantity")
    public ResponseEntity<Map<String, Object>> getMenuOptionsForQuantity(@PathVariable Long menuId) {
        List<MenuOption> options = menuOptionRepository.findByMenu_Id(menuId);
        List<Map<String, Object>> optionList = options.stream()
                .map(option -> {
                    Map<String, Object> optionMap = new HashMap<>();
                    optionMap.put("optionId", option.getId());
                    optionMap.put("name", option.getItemName());
                    optionMap.put("price", option.getItemPrice());
                    optionMap.put("defaultQty", option.getDefaultQty());
                    optionMap.put("currentQty", option.getDefaultQty()); // 현재 선택된 수량
                    return optionMap;
                })
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("options", optionList);
        
        return ResponseEntity.ok(response);
    }

    // 서빙 스타일 조회 (슬라이드 방식 포함)
    @GetMapping("/serving-styles")
    public ResponseEntity<Map<String, Object>> getServingStyles() {
        List<ServingStyle> servingStyles = servingStyleRepository.findAll();

        List<Map<String, Object>> styleList = servingStyles.stream()
                .map(style -> {
                    Map<String, Object> styleMap = new HashMap<>();
                    styleMap.put("styleId", style.getId());
                    styleMap.put("name", style.getName());
                    styleMap.put("extraPrice", style.getExtraPrice());
                    return styleMap;
                })
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("styles", styleList);
        
        return ResponseEntity.ok(response);
    }
}


