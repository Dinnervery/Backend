package com.dinnervery.backend.menu;

import com.dinnervery.backend.repository.MenuOptionRepository;
import com.dinnervery.backend.repository.MenuRepository;
import com.dinnervery.backend.repository.ServingStyleRepository;
import com.dinnervery.backend.entity.Menu;
import com.dinnervery.backend.entity.MenuOption;
import com.dinnervery.backend.entity.ServingStyle;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final ServingStyleRepository servingStyleRepository;
    private final MenuRepository menuRepository;
    private final MenuOptionRepository menuOptionRepository;

    @Override
    public void run(String... args) throws Exception {
        // 서빙 스타일 생성
        createServingStyles();
        
        // 메뉴 생성
        createMenus();
        
        // 메뉴 옵션 생성
        createMenuOptions();
        
        System.out.println("초기 데이터 로딩이 완료되었습니다.");
    }

    private void createServingStyles() {
        if (servingStyleRepository.count() == 0) {
            ServingStyle simple = ServingStyle.builder()
                    .name("SIMPLE")
                    .extraPrice(BigDecimal.ZERO)
                    .build();
            servingStyleRepository.save(simple);

            ServingStyle grand = ServingStyle.builder()
                    .name("GRAND")
                    .extraPrice(new BigDecimal("5000"))
                    .build();
            servingStyleRepository.save(grand);

            ServingStyle deluxe = ServingStyle.builder()
                    .name("DELUXE")
                    .extraPrice(new BigDecimal("10000"))
                    .build();
            servingStyleRepository.save(deluxe);
        }
    }

    private void createMenus() {
        if (menuRepository.count() == 0) {
            Menu valentineDinner = Menu.builder()
                    .name("발렌타인 디너")
                    .price(new BigDecimal("28000"))
                    .description("로맨틱한 발렌타인 특별 디너")
                    .build();
            menuRepository.save(valentineDinner);

            Menu englishDinner = Menu.builder()
                    .name("잉글리시 디너")
                    .price(new BigDecimal("35000"))
                    .description("영국 전통 디너 코스")
                    .build();
            menuRepository.save(englishDinner);

            Menu frenchDinner = Menu.builder()
                    .name("프렌치 디너")
                    .price(new BigDecimal("45000"))
                    .description("프랑스 고급 디너 코스")
                    .build();
            menuRepository.save(frenchDinner);

            Menu champagneDinner = Menu.builder()
                    .name("샴페인 축제 디너")
                    .price(new BigDecimal("90000"))
                    .description("프리미엄 샴페인과 함께하는 축제 디너")
                    .build();
            menuRepository.save(champagneDinner);
        }
    }

    private void createMenuOptions() {
        if (menuOptionRepository.count() == 0) {
            // 발렌타인 디너 옵션
            Menu valentineDinner = menuRepository.findByName("발렌타인 디너").orElseThrow();
            
            MenuOption valentineSteak = MenuOption.builder()
                    .menu(valentineDinner)
                    .itemName("스테이크")
                    .itemPrice(15000)
                    .defaultQty(1)
                    .build();
            menuOptionRepository.save(valentineSteak);

            MenuOption valentineWine = MenuOption.builder()
                    .menu(valentineDinner)
                    .itemName("와인")
                    .itemPrice(8000)
                    .build();
            menuOptionRepository.save(valentineWine);

            MenuOption valentineCoffee = MenuOption.builder()
                    .menu(valentineDinner)
                    .itemName("커피")
                    .itemPrice(5000)
                    .build();
            menuOptionRepository.save(valentineCoffee);

            MenuOption valentineSalad = MenuOption.builder()
                    .menu(valentineDinner)
                    .itemName("샐러드")
                    .itemPrice(7000)
                    .build();
            menuOptionRepository.save(valentineSalad);

            // 잉글리시 디너 옵션
            Menu englishDinner = menuRepository.findByName("잉글리시 디너").orElseThrow();
            
            MenuOption englishSteak = MenuOption.builder()
                    .menu(englishDinner)
                    .itemName("스테이크")
                    .itemPrice(15000)
                    .build();
            menuOptionRepository.save(englishSteak);

            MenuOption englishWine = MenuOption.builder()
                    .menu(englishDinner)
                    .itemName("와인")
                    .itemPrice(8000)
                    .build();
            menuOptionRepository.save(englishWine);

            MenuOption englishCoffee = MenuOption.builder()
                    .menu(englishDinner)
                    .itemName("커피")
                    .itemPrice(5000)
                    .build();
            menuOptionRepository.save(englishCoffee);

            MenuOption englishSalad = MenuOption.builder()
                    .menu(englishDinner)
                    .itemName("샐러드")
                    .itemPrice(7000)
                    .build();
            menuOptionRepository.save(englishSalad);

            MenuOption englishBaguette = MenuOption.builder()
                    .menu(englishDinner)
                    .itemName("바게트")
                    .itemPrice(3000)
                    .build();
            menuOptionRepository.save(englishBaguette);

            // 프렌치 디너 옵션
            Menu frenchDinner = menuRepository.findByName("프렌치 디너").orElseThrow();
            
            MenuOption frenchSteak = MenuOption.builder()
                    .menu(frenchDinner)
                    .itemName("스테이크")
                    .itemPrice(15000)
                    .build();
            menuOptionRepository.save(frenchSteak);

            MenuOption frenchWine = MenuOption.builder()
                    .menu(frenchDinner)
                    .itemName("와인")
                    .itemPrice(8000)
                    .build();
            menuOptionRepository.save(frenchWine);

            MenuOption frenchCoffee = MenuOption.builder()
                    .menu(frenchDinner)
                    .itemName("커피")
                    .itemPrice(5000)
                    .build();
            menuOptionRepository.save(frenchCoffee);

            MenuOption frenchSalad = MenuOption.builder()
                    .menu(frenchDinner)
                    .itemName("샐러드")
                    .itemPrice(7000)
                    .build();
            menuOptionRepository.save(frenchSalad);

            MenuOption frenchBaguette = MenuOption.builder()
                    .menu(frenchDinner)
                    .itemName("바게트")
                    .itemPrice(3000)
                    .build();
            menuOptionRepository.save(frenchBaguette);

            // 샴페인 축제 디너 옵션
            Menu champagneDinner = menuRepository.findByName("샴페인 축제 디너").orElseThrow();
            
            MenuOption champagneSteak = MenuOption.builder()
                    .menu(champagneDinner)
                    .itemName("스테이크")
                    .itemPrice(15000)
                    .build();
            menuOptionRepository.save(champagneSteak);

            MenuOption champagneWine = MenuOption.builder()
                    .menu(champagneDinner)
                    .itemName("와인")
                    .itemPrice(8000)
                    .build();
            menuOptionRepository.save(champagneWine);

            MenuOption champagneChampagne = MenuOption.builder()
                    .menu(champagneDinner)
                    .itemName("샴페인")
                    .itemPrice(25000)
                    .build();
            menuOptionRepository.save(champagneChampagne);

            MenuOption champagneCoffee = MenuOption.builder()
                    .menu(champagneDinner)
                    .itemName("커피")
                    .itemPrice(5000)
                    .build();
            menuOptionRepository.save(champagneCoffee);

            MenuOption champagneSalad = MenuOption.builder()
                    .menu(champagneDinner)
                    .itemName("샐러드")
                    .itemPrice(7000)
                    .build();
            menuOptionRepository.save(champagneSalad);

            MenuOption champagneBaguette = MenuOption.builder()
                    .menu(champagneDinner)
                    .itemName("바게트")
                    .itemPrice(3000)
                    .build();
            menuOptionRepository.save(champagneBaguette);

            MenuOption champagneCoffeePot = MenuOption.builder()
                    .menu(champagneDinner)
                    .itemName("커피포트")
                    .itemPrice(10000)
                    .build();
            menuOptionRepository.save(champagneCoffeePot);
        }
    }
}
