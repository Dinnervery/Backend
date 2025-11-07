package com.dinnervery.controller;

import com.dinnervery.entity.Menu;
import com.dinnervery.entity.MenuOption;
import com.dinnervery.entity.Storage;
import com.dinnervery.repository.MenuOptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StorageController.class)
@ActiveProfiles("test")
class StorageControllerWebTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MenuOptionRepository menuOptionRepository;

    private Menu menu;
    private Storage meatStorage;
    private Storage wineStorage;
    private MenuOption steakOption;
    private MenuOption wineOption;
    private MenuOption optionWithoutStorage;

    @BeforeEach
    void setUp() {
        // 메뉴 생성
        menu = Menu.builder()
                .name("발렌타인 디너")
                .price(28000)
                .build();
        ReflectionTestUtils.setField(menu, "id", 1L);

        // 재고 생성
        meatStorage = Storage.builder()
                .name("고기")
                .quantity(100)
                .build();
        ReflectionTestUtils.setField(meatStorage, "id", 1L);

        wineStorage = Storage.builder()
                .name("와인")
                .quantity(50)
                .build();
        ReflectionTestUtils.setField(wineStorage, "id", 2L);

        // 재고가 연결된 옵션
        steakOption = MenuOption.builder()
                .menu(menu)
                .name("스테이크")
                .price(15000)
                .defaultQty(1)
                .build();
        steakOption.setStorageItem(meatStorage);
        steakOption.setStorageConsumption(1);
        ReflectionTestUtils.setField(steakOption, "id", 1L);

        wineOption = MenuOption.builder()
                .menu(menu)
                .name("와인")
                .price(8000)
                .defaultQty(1)
                .build();
        wineOption.setStorageItem(wineStorage);
        wineOption.setStorageConsumption(1);
        ReflectionTestUtils.setField(wineOption, "id", 2L);

        // 재고가 연결되지 않은 옵션
        optionWithoutStorage = MenuOption.builder()
                .menu(menu)
                .name("추가 옵션")
                .price(5000)
                .defaultQty(1)
                .build();
        ReflectionTestUtils.setField(optionWithoutStorage, "id", 3L);

        // Mock 설정
        when(menuOptionRepository.findAll()).thenReturn(List.of(steakOption, wineOption, optionWithoutStorage));
    }

    @Test
    void getAllStorage_returnsOk() throws Exception {
        mockMvc.perform(get("/api/storage")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.storageItems").exists())
                .andExpect(jsonPath("$.storageItems").isArray())
                .andExpect(jsonPath("$.storageItems.length()").value(2)) // 재고가 연결된 옵션만 반환
                .andExpect(jsonPath("$.storageItems[0].optionId").exists())
                .andExpect(jsonPath("$.storageItems[0].optionName").exists())
                .andExpect(jsonPath("$.storageItems[0].quantity").exists());
    }

    @Test
    void getAllStorage_filtersOptionsWithoutStorage() throws Exception {
        mockMvc.perform(get("/api/storage")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.storageItems").isArray())
                .andExpect(jsonPath("$.storageItems.length()").value(2)) // 재고가 없는 옵션은 제외
                .andExpect(jsonPath("$.storageItems[?(@.optionId == 1)]").exists()) // 스테이크
                .andExpect(jsonPath("$.storageItems[?(@.optionId == 2)]").exists()) // 와인
                .andExpect(jsonPath("$.storageItems[?(@.optionId == 3)]").doesNotExist()); // 재고 없는 옵션 제외
    }

    @Test
    void getAllStorage_returnsCorrectQuantity() throws Exception {
        mockMvc.perform(get("/api/storage")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.storageItems[?(@.optionId == 1)].quantity").value(100)) // 고기 재고
                .andExpect(jsonPath("$.storageItems[?(@.optionId == 2)].quantity").value(50)); // 와인 재고
    }

    @Test
    void getAllStorage_returnsEmptyListWhenNoStorageItems() throws Exception {
        when(menuOptionRepository.findAll()).thenReturn(List.of(optionWithoutStorage));

        mockMvc.perform(get("/api/storage")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.storageItems").isArray())
                .andExpect(jsonPath("$.storageItems.length()").value(0));
    }
}

