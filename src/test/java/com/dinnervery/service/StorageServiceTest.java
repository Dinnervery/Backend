package com.dinnervery.service;

import com.dinnervery.entity.Menu;
import com.dinnervery.entity.MenuOption;
import com.dinnervery.entity.Storage;
import com.dinnervery.repository.StorageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StorageServiceTest {

    @Mock
    private StorageRepository storageRepository;

    @InjectMocks
    private StorageService storageService;

    private Menu menu;
    private Storage meatStorage;
    private Storage wineStorage;
    private MenuOption steakOption;
    private MenuOption wineOption;
    private MenuOption optionWithoutStorage;

    @BeforeEach
    void setUp() {
        menu = Menu.builder()
                .name("발렌타인 디너")
                .price(28000)
                .build();

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

        steakOption = MenuOption.builder()
                .menu(menu)
                .name("스테이크")
                .price(15000)
                .defaultQty(1)
                .build();
        steakOption.setStorageItem(meatStorage);
        steakOption.setStorageConsumption(1);

        wineOption = MenuOption.builder()
                .menu(menu)
                .name("와인")
                .price(8000)
                .defaultQty(1)
                .build();
        wineOption.setStorageItem(wineStorage);
        wineOption.setStorageConsumption(1);

        optionWithoutStorage = MenuOption.builder()
                .menu(menu)
                .name("추가 옵션")
                .price(5000)
                .defaultQty(1)
                .build();
    }

    @Test
    void checkStock_성공_재고충분() {
        // given - 재고가 충분한 경우
        // when & then - 예외가 발생하지 않아야 함
        assertThat(meatStorage.getQuantity()).isGreaterThanOrEqualTo(steakOption.getStorageConsumption());
        storageService.checkStock(steakOption, 1);
    }

    @Test
    void checkStock_실패_재고부족() {
        // given - 재고가 부족한 경우
        meatStorage.setQuantity(0);

        // when & then - 예외 발생
        assertThatThrownBy(() -> storageService.checkStock(steakOption, 1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("재고가 부족합니다");
    }

    @Test
    void checkStock_재고미연결옵션_예외없음() {
        // given - 재고가 연결되지 않은 옵션
        // when & then - 예외가 발생하지 않아야 함
        storageService.checkStock(optionWithoutStorage, 1);
    }

    @Test
    void checkStock_수량계산_정확() {
        // given - 수량 2개 주문, 소비량 1
        steakOption.setStorageConsumption(1);
        meatStorage.setQuantity(100);

        // when - 수량 2개 체크
        storageService.checkStock(steakOption, 2);

        // then - 예외 없음 (100 >= 1 * 2)
        verify(storageRepository, never()).save(any());
    }

    @Test
    void checkStock_수량계산_부족() {
        // given - 수량 3개 주문, 소비량 1, 재고 2
        steakOption.setStorageConsumption(1);
        meatStorage.setQuantity(2);

        // when & then - 예외 발생 (2 < 1 * 3)
        assertThatThrownBy(() -> storageService.checkStock(steakOption, 3))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("재고가 부족합니다");
    }

    @Test
    void deductStock_성공() {
        // given
        meatStorage.setQuantity(100);
        when(storageRepository.save(any(Storage.class))).thenReturn(meatStorage);

        // when
        storageService.deductStock(steakOption, 2);

        // then - 재고가 차감되어야 함 (100 - 1 * 2 = 98)
        assertThat(meatStorage.getQuantity()).isEqualTo(98);
        verify(storageRepository).save(meatStorage);
    }

    @Test
    void deductStock_재고미연결옵션_차감안함() {
        // given - 재고가 연결되지 않은 옵션
        // when
        storageService.deductStock(optionWithoutStorage, 1);

        // then - 저장 호출 안됨
        verify(storageRepository, never()).save(any());
    }

    @Test
    void deductStock_소비량반영() {
        // given - 소비량 2, 수량 3개 주문
        steakOption.setStorageConsumption(2);
        meatStorage.setQuantity(100);
        when(storageRepository.save(any(Storage.class))).thenReturn(meatStorage);

        // when
        storageService.deductStock(steakOption, 3);

        // then - 재고가 차감되어야 함 (100 - 2 * 3 = 94)
        assertThat(meatStorage.getQuantity()).isEqualTo(94);
        verify(storageRepository).save(meatStorage);
    }

    @Test
    void resetDailyStock_성공() {
        // given
        Storage storage1 = Storage.builder().name("고기").quantity(10).build();
        Storage storage2 = Storage.builder().name("와인").quantity(20).build();
        when(storageRepository.findAll()).thenReturn(List.of(storage1, storage2));
        when(storageRepository.saveAll(anyList())).thenReturn(List.of(storage1, storage2));

        // when
        storageService.resetDailyStock();

        // then - 모든 재고가 100으로 리셋
        assertThat(storage1.getQuantity()).isEqualTo(100);
        assertThat(storage2.getQuantity()).isEqualTo(100);
        verify(storageRepository).saveAll(anyList());
    }
}

