package com.dinnervery.config;

import com.dinnervery.repository.MenuOptionRepository;
import com.dinnervery.repository.MenuRepository;
import com.dinnervery.repository.ServingStyleRepository;
import com.dinnervery.repository.EmployeeRepository;
import com.dinnervery.entity.Menu;
import com.dinnervery.entity.MenuOption;
import com.dinnervery.entity.ServingStyle;
import com.dinnervery.entity.Employee;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "spring.profiles.active", havingValue = "prod", matchIfMissing = false)
public class DataLoader implements CommandLineRunner {

    private final ServingStyleRepository servingStyleRepository;
    private final MenuRepository menuRepository;
    private final MenuOptionRepository menuOptionRepository;
    private final EmployeeRepository employeeRepository;

    @Override
    public void run(String... args) throws Exception {
        // 서빙 스타일 생성
        createServingStyles();
        
        // 메뉴 ?�성
        createMenus();
        
        // 메뉴 옵션 생성
        createMenuOptions();
        
        // 직원 계정 ?�성
        createEmployees();
        
        System.out.println("초기 ?�이??로딩???�료?�었?�니??");
    }

    private void createServingStyles() {
        if (servingStyleRepository.count() == 0) {
            ServingStyle simple = ServingStyle.builder()
                    .name("SIMPLE")
                    .extraPrice(0)
                    .build();
            servingStyleRepository.save(simple);

            ServingStyle grand = ServingStyle.builder()
                    .name("GRAND")
                    .extraPrice(5000)
                    .build();
            servingStyleRepository.save(grand);

            ServingStyle deluxe = ServingStyle.builder()
                    .name("DELUXE")
                    .extraPrice(10000)
                    .build();
            servingStyleRepository.save(deluxe);
        }
    }

    private void createMenus() {
        if (menuRepository.count() == 0) {
            Menu valentineDinner = Menu.builder()
                    .name("발렌?�???�너")
                    .price(28000)
                    .description("?�테?�크, ?�?? ?�피???�식 ?�시, ?�킨")
                    .build();
            menuRepository.save(valentineDinner);

            Menu englishDinner = Menu.builder()
                    .name("?��?리시 ?�너")
                    .price(35000)
                    .description("?�그 ?�크?�블, 베이�? 바게?�빵, ?�테?�크")
                    .build();
            menuRepository.save(englishDinner);

            Menu frenchDinner = Menu.builder()
                    .name("?�렌�??�너")
                    .price(45000)
                    .description("커피, ?�?? ?�러?? ?�테?�크")
                    .build();
            menuRepository.save(frenchDinner);

            Menu champagneDinner = Menu.builder()
                    .name("?�페??축제 ?�너")
                    .price(90000)
                    .description("?�페??2�? 바게?�빵 4�? 커피?�트, ?�?? ?�테?�크")
                    .build();
            menuRepository.save(champagneDinner);
        }
    }

    private void createMenuOptions() {
        if (menuOptionRepository.count() == 0) {
            // 발렌?�???�너 ?�션 (?�테?�크, ?�??
            Menu valentineDinner = menuRepository.findByName("발렌?�???�너").orElseThrow();
            
            MenuOption valentineSteak = MenuOption.builder()
                    .menu(valentineDinner)
                    .itemName("?�테?�크")
                    .itemPrice(15000)
                    .defaultQty(1)
                    .build();
            menuOptionRepository.save(valentineSteak);

            MenuOption valentineWine = MenuOption.builder()
                    .menu(valentineDinner)
                    .itemName("와인")
                    .itemPrice(8000)
                    .defaultQty(1)
                    .build();
            menuOptionRepository.save(valentineWine);

            // ?��?리시 ?�너 ?�션 (?�그 ?�크?�블, 베이�? 바게?�빵, ?�테?�크)
            Menu englishDinner = menuRepository.findByName("?��?리시 ?�너").orElseThrow();
            
            MenuOption englishEggScramble = MenuOption.builder()
                    .menu(englishDinner)
                    .itemName("?�그 ?�크?�블")
                    .itemPrice(5000)
                    .defaultQty(1)
                    .build();
            menuOptionRepository.save(englishEggScramble);

            MenuOption englishBacon = MenuOption.builder()
                    .menu(englishDinner)
                    .itemName("베이컨")
                    .itemPrice(4000)
                    .defaultQty(1)
                    .build();
            menuOptionRepository.save(englishBacon);

            MenuOption englishBaguette = MenuOption.builder()
                    .menu(englishDinner)
                    .itemName("바게?�빵")
                    .itemPrice(3000)
                    .defaultQty(1)
                    .build();
            menuOptionRepository.save(englishBaguette);

            MenuOption englishSteak = MenuOption.builder()
                    .menu(englishDinner)
                    .itemName("?�테?�크")
                    .itemPrice(15000)
                    .defaultQty(1)
                    .build();
            menuOptionRepository.save(englishSteak);

            // ?�렌�??�너 ?�션 (커피, ?�?? ?�러?? ?�테?�크)
            Menu frenchDinner = menuRepository.findByName("?�렌�??�너").orElseThrow();
            
            MenuOption frenchCoffee = MenuOption.builder()
                    .menu(frenchDinner)
                    .itemName("커피")
                    .itemPrice(5000)
                    .defaultQty(1)
                    .build();
            menuOptionRepository.save(frenchCoffee);

            MenuOption frenchWine = MenuOption.builder()
                    .menu(frenchDinner)
                    .itemName("와인")
                    .itemPrice(8000)
                    .defaultQty(1)
                    .build();
            menuOptionRepository.save(frenchWine);

            MenuOption frenchSalad = MenuOption.builder()
                    .menu(frenchDinner)
                    .itemName("샐러드")
                    .itemPrice(7000)
                    .defaultQty(1)
                    .build();
            menuOptionRepository.save(frenchSalad);

            MenuOption frenchSteak = MenuOption.builder()
                    .menu(frenchDinner)
                    .itemName("?�테?�크")
                    .itemPrice(15000)
                    .defaultQty(1)
                    .build();
            menuOptionRepository.save(frenchSteak);

            // ?�페??축제 ?�너 ?�션 (?�페??2�? 바게?�빵 4�? 커피?�트, ?�?? ?�테?�크)
            Menu champagneDinner = menuRepository.findByName("?�페??축제 ?�너").orElseThrow();
            
            MenuOption champagneChampagne = MenuOption.builder()
                    .menu(champagneDinner)
                    .itemName("샴페인")
                    .itemPrice(25000)
                    .defaultQty(2) // 기본 2�?
                    .build();
            menuOptionRepository.save(champagneChampagne);

            MenuOption champagneBaguette = MenuOption.builder()
                    .menu(champagneDinner)
                    .itemName("바게?�빵")
                    .itemPrice(3000)
                    .defaultQty(4) // 기본 4�?
                    .build();
            menuOptionRepository.save(champagneBaguette);

            MenuOption champagneCoffeePot = MenuOption.builder()
                    .menu(champagneDinner)
                    .itemName("커피?�트")
                    .itemPrice(10000)
                    .defaultQty(1)
                    .build();
            menuOptionRepository.save(champagneCoffeePot);

            MenuOption champagneWine = MenuOption.builder()
                    .menu(champagneDinner)
                    .itemName("와인")
                    .itemPrice(8000)
                    .defaultQty(1)
                    .build();
            menuOptionRepository.save(champagneWine);

            MenuOption champagneSteak = MenuOption.builder()
                    .menu(champagneDinner)
                    .itemName("?�테?�크")
                    .itemPrice(15000)
                    .defaultQty(1)
                    .build();
            menuOptionRepository.save(champagneSteak);
        }
    }

    private void createEmployees() {
        if (employeeRepository.count() == 0) {
            // ?�리??계정 ?�성
            Employee cook = Employee.builder()
                    .loginId("cook")
                    .password("cook123")
                    .name("요리사")
                    .phoneNumber("010-1111-1111")
                    .task(Employee.EmployeeTask.COOK)
                    .build();
            employeeRepository.save(cook);

            // 배달??계정 ?�성
            Employee deliveryPerson = Employee.builder()
                    .loginId("delivery")
                    .password("delivery123")
                    .name("배달원")
                    .phoneNumber("010-2222-2222")
                    .task(Employee.EmployeeTask.DELIVERY)
                    .build();
            employeeRepository.save(deliveryPerson);
        }
    }
}
