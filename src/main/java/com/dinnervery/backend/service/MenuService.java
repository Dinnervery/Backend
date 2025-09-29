package com.dinnervery.backend.service;

import com.dinnervery.backend.dto.MenuDto;
import com.dinnervery.backend.dto.request.MenuCreateRequest;
import com.dinnervery.backend.entity.Menu;
import com.dinnervery.backend.repository.MenuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MenuService {

    private final MenuRepository menuRepository;

    @Transactional
    public MenuDto createMenu(MenuCreateRequest request) {
        Menu menu = Menu.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .build();

        Menu savedMenu = menuRepository.save(menu);
        return MenuDto.from(savedMenu);
    }

    public MenuDto getMenuById(Long id) {
        Menu menu = menuRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("메뉴를 찾을 수 없습니다: " + id));
        return MenuDto.from(menu);
    }

    public List<MenuDto> getAllMenus() {
        return menuRepository.findAll().stream()
                .map(MenuDto::from)
                .collect(Collectors.toList());
    }





    @Transactional
    public void deleteMenu(Long id) {
        if (!menuRepository.existsById(id)) {
            throw new IllegalArgumentException("메뉴를 찾을 수 없습니다: " + id);
        }
        menuRepository.deleteById(id);
    }
}


