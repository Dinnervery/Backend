package com.dinnervery.backend.test;

import com.dinnervery.backend.entity.Customer;
import com.dinnervery.backend.entity.Menu;
import com.dinnervery.backend.entity.ServingStyle;
import com.dinnervery.backend.repository.CustomerRepository;
import com.dinnervery.backend.repository.MenuRepository;
import com.dinnervery.backend.repository.ServingStyleRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class DatabaseTest {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private MenuRepository menuRepository;

    @Autowired
    private ServingStyleRepository servingStyleRepository;

    @Test
    void 데이터베이스에_데이터_삽입_및_조회_테스트() {
        // 고객 생성
        Customer customer = Customer.builder()
                .loginId("test_user_001")
                .password("password123")
                .name("테스트 고객")
                .phoneNumber("010-1234-5678")
                .build();
        customer = customerRepository.save(customer);

        // 메뉴 생성
        Menu menu = Menu.builder()
                .name("발렌타인 디너")
                .price(28000)
                .description("로맨틱한 발렌타인 특별 디너")
                .build();
        menu = menuRepository.save(menu);

        // 서빙 스타일 생성
        ServingStyle servingStyle = ServingStyle.builder()
                .name("심플 스타일")
                .extraPrice(0)
                .build();
        servingStyle = servingStyleRepository.save(servingStyle);

        // 데이터 검증
        assertThat(customer.getId()).isNotNull();
        assertThat(customer.getLoginId()).isEqualTo("test_user_001");
        assertThat(customer.getName()).isEqualTo("테스트 고객");

        assertThat(menu.getId()).isNotNull();
        assertThat(menu.getName()).isEqualTo("발렌타인 디너");
        assertThat(menu.getPrice()).isEqualTo(28000);

        assertThat(servingStyle.getId()).isNotNull();
        assertThat(servingStyle.getName()).isEqualTo("심플 스타일");
        assertThat(servingStyle.getExtraPrice()).isEqualTo(0);

        // 전체 조회 테스트
        List<Customer> customers = customerRepository.findAll();
        List<Menu> menus = menuRepository.findAll();
        List<ServingStyle> servingStyles = servingStyleRepository.findAll();

        assertThat(customers).hasSize(1);
        assertThat(menus).hasSize(1);
        assertThat(servingStyles).hasSize(1);

        System.out.println("=== 데이터베이스 테스트 성공 ===");
        System.out.println("고객 수: " + customers.size());
        System.out.println("메뉴 수: " + menus.size());
        System.out.println("서빙 스타일 수: " + servingStyles.size());
    }
}
