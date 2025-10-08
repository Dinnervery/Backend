package com.dinnervery.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dinnervery.entity.MenuOption;

import java.util.List;

public interface MenuOptionRepository extends JpaRepository<MenuOption, Long> {
    List<MenuOption> findByMenu_Id(Long menuId);
}


