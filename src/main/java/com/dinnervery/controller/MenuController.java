package com.dinnervery.controller;

import com.dinnervery.entity.Menu;
import com.dinnervery.entity.MenuOption;
import com.dinnervery.entity.Style;
import com.dinnervery.repository.MenuOptionRepository;
import com.dinnervery.repository.MenuRepository;
import com.dinnervery.repository.StyleRepository;
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
    private final StyleRepository styleRepository;

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
                    
                    return menuMap;
                })
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("menus", menuList);
        
        return ResponseEntity.ok(response);
    }


    // 구성품 조회
    @GetMapping("/menus/{menuId}/options")
    public ResponseEntity<Map<String, Object>> getMenuOptions(@PathVariable Long menuId) {
        List<MenuOption> options = menuOptionRepository.findByMenu_Id(menuId);
        List<Map<String, Object>> optionList = options.stream()
                .map(option -> {
                    Map<String, Object> optionMap = new HashMap<>();
                    optionMap.put("optionId", option.getId());
                    optionMap.put("name", option.getName());
                    optionMap.put("price", option.getPrice());
                    optionMap.put("defaultQty", option.getDefaultQty());
                    optionMap.put("quantity", option.getDefaultQty()); // 현재 수량(변경O)
                    return optionMap;
                })
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("options", optionList);
        
        return ResponseEntity.ok(response);
    }

    // 스타일 조회
    @GetMapping("/styles")
    public ResponseEntity<Map<String, Object>> getStyles() {
        List<Style> styles = styleRepository.findAll();

        List<Map<String, Object>> styleList = styles.stream()
                .map(style -> {
                    Map<String, Object> styleMap = new HashMap<>();
                    styleMap.put("styleId", style.getId());
                    styleMap.put("name", style.getName());
                    styleMap.put("price", style.getExtraPrice());
                    return styleMap;
                })
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("styles", styleList);
        
        return ResponseEntity.ok(response);
    }
}


