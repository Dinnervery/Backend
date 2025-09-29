package com.dinnervery.backend.controller;

import com.dinnervery.backend.dto.MenuDto;
import com.dinnervery.backend.dto.request.MenuCreateRequest;
import com.dinnervery.backend.entity.Menu;
import com.dinnervery.backend.entity.MenuOption;
import com.dinnervery.backend.entity.ServingStyle;
import com.dinnervery.backend.repository.MenuOptionRepository;
import com.dinnervery.backend.repository.MenuRepository;
import com.dinnervery.backend.repository.ServingStyleRepository;
import com.dinnervery.backend.service.MenuService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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

    private final MenuService menuService;
    private final MenuRepository menuRepository;
    private final MenuOptionRepository menuOptionRepository;
    private final ServingStyleRepository servingStyleRepository;

    // 메뉴 CRUD 기능
    @PostMapping("/menus")
    public ResponseEntity<MenuDto> createMenu(@Valid @RequestBody MenuCreateRequest request) {
        MenuDto menu = menuService.createMenu(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(menu);
    }

    @GetMapping("/menus/{id}")
    public ResponseEntity<MenuDto> getMenuById(@PathVariable Long id) {
        MenuDto menu = menuService.getMenuById(id);
        return ResponseEntity.ok(menu);
    }

    @GetMapping("/menus")
    public ResponseEntity<List<MenuDto>> getAllMenus() {
        List<MenuDto> menus = menuService.getAllMenus();
        return ResponseEntity.ok(menus);
    }





    @DeleteMapping("/menus/{id}")
    public ResponseEntity<Void> deleteMenu(@PathVariable Long id) {
        menuService.deleteMenu(id);
        return ResponseEntity.noContent().build();
    }

    // 메뉴 상세 정보 조회 (옵션 포함)
    @GetMapping("/menus/detail/{id}")
    public ResponseEntity<Map<String, Object>> getMenuDetailById(@PathVariable Long id) {
        Menu menu = menuRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("메뉴를 찾을 수 없습니다: " + id));

        Map<String, Object> response = new HashMap<>();
        response.put("menuId", menu.getId());
        response.put("name", menu.getName());
        response.put("price", menu.getPrice());
        response.put("description", menu.getDescription());
        
        // 메뉴 옵션 조회
        List<MenuOption> options = menuOptionRepository.findByMenu_Id(menu.getId());
        List<Map<String, Object>> optionList = options.stream()
                .map(option -> {
                    Map<String, Object> optionMap = new HashMap<>();
                    optionMap.put("optionId", option.getId());
                    optionMap.put("name", option.getItemName());
                    optionMap.put("price", option.getItemPrice());
                    return optionMap;
                })
                .collect(Collectors.toList());
        response.put("options", optionList);

        return ResponseEntity.ok(response);
    }

    // 모든 메뉴 상세 정보 조회 (옵션 포함)
    @GetMapping("/menus/detail")
    public ResponseEntity<List<Map<String, Object>>> getAllMenusDetail() {
        List<Menu> menus = menuRepository.findAll();

        List<Map<String, Object>> response = menus.stream()
                .map(menu -> {
                    Map<String, Object> menuMap = new HashMap<>();
                    menuMap.put("menuId", menu.getId());
                    menuMap.put("name", menu.getName());
                    menuMap.put("price", menu.getPrice());
                    menuMap.put("description", menu.getDescription());
                    
                    // 메뉴 옵션 조회
                    List<MenuOption> options = menuOptionRepository.findByMenu_Id(menu.getId());
                    List<Map<String, Object>> optionList = options.stream()
                            .map(option -> {
                                Map<String, Object> optionMap = new HashMap<>();
                                optionMap.put("optionId", option.getId());
                                optionMap.put("name", option.getItemName());
                                optionMap.put("price", option.getItemPrice());
                                return optionMap;
                            })
                            .collect(Collectors.toList());
                    menuMap.put("options", optionList);
                    
                    return menuMap;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    // 서빙 스타일 조회
    @GetMapping("/serving-styles")
    public ResponseEntity<List<Map<String, Object>>> getServingStyles() {
        List<ServingStyle> servingStyles = servingStyleRepository.findAll();

        List<Map<String, Object>> response = servingStyles.stream()
                .map(style -> {
                    Map<String, Object> styleMap = new HashMap<>();
                    styleMap.put("servingStyleId", style.getId());
                    styleMap.put("name", style.getName());
                    styleMap.put("extraPrice", style.getExtraPrice());
                    return styleMap;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }
}


