package com.dinnervery.service;

import com.dinnervery.dto.MenuDto;
import com.dinnervery.entity.Menu;
import com.dinnervery.repository.MenuRepository;
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
}


