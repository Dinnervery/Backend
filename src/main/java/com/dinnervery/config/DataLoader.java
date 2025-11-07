package com.dinnervery.config;

import com.dinnervery.repository.MenuOptionRepository;
import com.dinnervery.repository.MenuRepository;
import com.dinnervery.repository.StyleRepository;
import com.dinnervery.repository.StorageRepository;
import com.dinnervery.repository.StaffRepository;
import com.dinnervery.entity.Menu;
import com.dinnervery.entity.MenuOption;
import com.dinnervery.entity.Style;
import com.dinnervery.entity.Storage;
import com.dinnervery.entity.Staff;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "spring.profiles.active", havingValue = "dev", matchIfMissing = false)
public class DataLoader implements CommandLineRunner {

    private final StyleRepository styleRepository;
    private final MenuRepository menuRepository;
    private final MenuOptionRepository menuOptionRepository;
    private final StorageRepository storageRepository;
    private final StaffRepository staffRepository;

    @Override
    public void run(String... args) throws Exception {
        // 스타일 생성
        createStyles();
        
        // 메뉴 생성
        createMenus();
        
        // 메뉴 옵션 생성
        createStorages();
        createMenuOptions();
        
        // 스태프 계정 생성
        createStaff();
        
        System.out.println("초기 데이터 로딩이 완료되었습니다.");
    }

    private void createStyles() {
        if (styleRepository.count() == 0) {
            Style simple = Style.builder()
                    .name("SIMPLE")
                    .extraPrice(0)
                    .build();
            styleRepository.save(simple);

            Style grand = Style.builder()
                    .name("GRAND")
                    .extraPrice(5000)
                    .build();
            styleRepository.save(grand);

            Style deluxe = Style.builder()
                    .name("DELUXE")
                    .extraPrice(10000)
                    .build();
            styleRepository.save(deluxe);
        }
    }

    private void createMenus() {
        if (menuRepository.count() == 0) {
            Menu valentineDinner = Menu.builder()
                    .name("발렌타인 디너")
                    .price(28000)
                    .build();
            menuRepository.save(valentineDinner);

            Menu englishDinner = Menu.builder()
                    .name("잉글리시 디너")
                    .price(35000)
                    .build();
            menuRepository.save(englishDinner);

            Menu frenchDinner = Menu.builder()
                    .name("프렌치 디너")
                    .price(45000)
                    .build();
            menuRepository.save(frenchDinner);

            Menu champagneDinner = Menu.builder()
                    .name("샴페인 축제 디너")
                    .price(90000)
                    .build();
            menuRepository.save(champagneDinner);
        }
    }

    private void createStorages() {
        if (storageRepository.count() == 0) {
            Storage meat = Storage.builder().name("고기").quantity(100).build();
            Storage wine = Storage.builder().name("와인").quantity(100).build();
            Storage veggie = Storage.builder().name("채소").quantity(100).build();
            Storage coffee = Storage.builder().name("커피").quantity(100).build();
            Storage champagne = Storage.builder().name("샴페인").quantity(100).build();
            Storage baguette = Storage.builder().name("바게트빵").quantity(100).build();
            Storage egg = Storage.builder().name("계란").quantity(100).build();
            storageRepository.save(meat);
            storageRepository.save(wine);
            storageRepository.save(veggie);
            storageRepository.save(coffee);
            storageRepository.save(champagne);
            storageRepository.save(baguette);
            storageRepository.save(egg);
        }
    }

    private void createMenuOptions() {
        if (menuOptionRepository.count() == 0) {
            // 발렌타인 디너 옵션 (스테이크, 와인)
            Menu valentineDinner = menuRepository.findByName("발렌타인 디너").orElseThrow();
            
            MenuOption valentineSteak = MenuOption.builder()
                    .menu(valentineDinner)
                    .name("스테이크")
                    .price(15000)
                    .defaultQty(1)
                    .build();
            valentineSteak.setStorageItem(storageRepository.findByName("고기").orElseThrow());
            valentineSteak.setStorageConsumption(1);
            menuOptionRepository.save(valentineSteak);

            MenuOption valentineWine = MenuOption.builder()
                    .menu(valentineDinner)
                    .name("와인")
                    .price(8000)
                    .defaultQty(1)
                    .build();
            valentineWine.setStorageItem(storageRepository.findByName("와인").orElseThrow());
            valentineWine.setStorageConsumption(1);
            menuOptionRepository.save(valentineWine);

            // 잉글리시 디너 옵션 (에그 스크램블, 베이컨, 바게트빵, 스테이크)
            Menu englishDinner = menuRepository.findByName("잉글리시 디너").orElseThrow();
            
            MenuOption englishEggScramble = MenuOption.builder()
                    .menu(englishDinner)
                    .name("에그 스크램블")
                    .price(5000)
                    .defaultQty(1)
                    .build();
            englishEggScramble.setStorageItem(storageRepository.findByName("계란").orElseThrow());
            englishEggScramble.setStorageConsumption(1);
            menuOptionRepository.save(englishEggScramble);

            MenuOption englishBacon = MenuOption.builder()
                    .menu(englishDinner)
                    .name("베이컨")
                    .price(4000)
                    .defaultQty(1)
                    .build();
            englishBacon.setStorageItem(storageRepository.findByName("고기").orElseThrow());
            englishBacon.setStorageConsumption(1);
            menuOptionRepository.save(englishBacon);

            MenuOption englishBaguette = MenuOption.builder()
                    .menu(englishDinner)
                    .name("바게트빵")
                    .price(3000)
                    .defaultQty(1)
                    .build();
            englishBaguette.setStorageItem(storageRepository.findByName("바게트빵").orElseThrow());
            englishBaguette.setStorageConsumption(1);
            menuOptionRepository.save(englishBaguette);

            MenuOption englishSteak = MenuOption.builder()
                    .menu(englishDinner)
                    .name("스테이크")
                    .price(15000)
                    .defaultQty(1)
                    .build();
            englishSteak.setStorageItem(storageRepository.findByName("고기").orElseThrow());
            englishSteak.setStorageConsumption(1);
            menuOptionRepository.save(englishSteak);

            // 프렌치 디너 옵션 (커피, 와인, 샐러드, 스테이크)
            Menu frenchDinner = menuRepository.findByName("프렌치 디너").orElseThrow();
            
            MenuOption frenchCoffee = MenuOption.builder()
                    .menu(frenchDinner)
                    .name("커피")
                    .price(5000)
                    .defaultQty(1)
                    .build();
            frenchCoffee.setStorageItem(storageRepository.findByName("커피").orElseThrow());
            frenchCoffee.setStorageConsumption(1);
            menuOptionRepository.save(frenchCoffee);

            MenuOption frenchWine = MenuOption.builder()
                    .menu(frenchDinner)
                    .name("와인")
                    .price(8000)
                    .defaultQty(1)
                    .build();
            frenchWine.setStorageItem(storageRepository.findByName("와인").orElseThrow());
            frenchWine.setStorageConsumption(1);
            menuOptionRepository.save(frenchWine);

            MenuOption frenchSalad = MenuOption.builder()
                    .menu(frenchDinner)
                    .name("샐러드")
                    .price(7000)
                    .defaultQty(1)
                    .build();
            frenchSalad.setStorageItem(storageRepository.findByName("채소").orElseThrow());
            frenchSalad.setStorageConsumption(1);
            menuOptionRepository.save(frenchSalad);

            MenuOption frenchSteak = MenuOption.builder()
                    .menu(frenchDinner)
                    .name("스테이크")
                    .price(15000)
                    .defaultQty(1)
                    .build();
            frenchSteak.setStorageItem(storageRepository.findByName("고기").orElseThrow());
            frenchSteak.setStorageConsumption(1);
            menuOptionRepository.save(frenchSteak);

            // 샴페인 축제 디너 옵션 (샴페인 2병, 바게트빵 4개, 커피포트, 와인, 스테이크)
            Menu champagneDinner = menuRepository.findByName("샴페인 축제 디너").orElseThrow();
            
            MenuOption champagneChampagne = MenuOption.builder()
                    .menu(champagneDinner)
                    .name("샴페인")
                    .price(25000)
                    .defaultQty(2) // 기본 2병
                    .build();
            champagneChampagne.setStorageItem(storageRepository.findByName("샴페인").orElseThrow());
            champagneChampagne.setStorageConsumption(2);
            menuOptionRepository.save(champagneChampagne);

            MenuOption champagneBaguette = MenuOption.builder()
                    .menu(champagneDinner)
                    .name("바게트빵")
                    .price(3000)
                    .defaultQty(4) // 기본 4개
                    .build();
            champagneBaguette.setStorageItem(storageRepository.findByName("바게트빵").orElseThrow());
            champagneBaguette.setStorageConsumption(4);
            menuOptionRepository.save(champagneBaguette);

            MenuOption champagneCoffeePot = MenuOption.builder()
                    .menu(champagneDinner)
                    .name("커피포트")
                    .price(10000)
                    .defaultQty(1)
                    .build();
            champagneCoffeePot.setStorageItem(storageRepository.findByName("커피").orElseThrow());
            champagneCoffeePot.setStorageConsumption(5);
            menuOptionRepository.save(champagneCoffeePot);

            MenuOption champagneWine = MenuOption.builder()
                    .menu(champagneDinner)
                    .name("와인")
                    .price(8000)
                    .defaultQty(1)
                    .build();
            champagneWine.setStorageItem(storageRepository.findByName("와인").orElseThrow());
            champagneWine.setStorageConsumption(1);
            menuOptionRepository.save(champagneWine);

            MenuOption champagneSteak = MenuOption.builder()
                    .menu(champagneDinner)
                    .name("스테이크")
                    .price(15000)
                    .defaultQty(1)
                    .build();
            champagneSteak.setStorageItem(storageRepository.findByName("고기").orElseThrow());
            champagneSteak.setStorageConsumption(1);
            menuOptionRepository.save(champagneSteak);
        }
    }

    private void createStaff() {
        if (staffRepository.count() == 0) {
            // 요리사 계정 생성
            Staff cook = Staff.builder()
                    .loginId("cook")
                    .password("cook123")
                    .name("요리사")
                    .phoneNumber("010-1111-1111")
                    .task(Staff.StaffTask.COOK)
                    .build();
            staffRepository.save(cook);

            // 배달원 계정 생성
            Staff deliveryPerson = Staff.builder()
                    .loginId("delivery")
                    .password("delivery123")
                    .name("배달원")
                    .phoneNumber("010-2222-2222")
                    .task(Staff.StaffTask.DELIVERY)
                    .build();
            staffRepository.save(deliveryPerson);
        }
    }
}
