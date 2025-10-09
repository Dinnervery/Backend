package com.dinnervery.order;

import com.dinnervery.entity.Customer;
import com.dinnervery.entity.Menu;
import com.dinnervery.entity.ServingStyle;
import com.dinnervery.repository.CustomerRepository;
import com.dinnervery.repository.MenuRepository;
import com.dinnervery.repository.ServingStyleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Transactional
class SimpleOrderTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private MenuRepository menuRepository;

    @Autowired
    private ServingStyleRepository servingStyleRepository;

    private Customer testCustomer;
    private Menu testMenu;
    private ServingStyle testServingStyle;

    @BeforeEach
    void setUp() {
        // 간단한 테스트 데이터 생성
        testCustomer = Customer.builder()
                .loginId("simple_test_" + System.currentTimeMillis())
                .password("password")
                .name("간단 테스트 고객")
                .phoneNumber("010-0000-0000")
                .build();
        testCustomer = customerRepository.save(testCustomer);

        testMenu = Menu.builder()
                .name("간단 디너")
                .price(20000)
                .description("간단한 테스트용 디너")
                .build();
        testMenu = menuRepository.save(testMenu);

        testServingStyle = ServingStyle.builder()
                .name("기본_" + System.currentTimeMillis())
                .extraPrice(0)
                .build();
        testServingStyle = servingStyleRepository.save(testServingStyle);
    }

    @Test
    void testBusinessHoursRetrieval() throws Exception {
        // when & then - 영업시간 조회 테스트
        mockMvc.perform(get("/api/business-hours/status")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.openTime").value("15:30"))
                .andExpect(jsonPath("$.closeTime").value("22:00"))
                .andExpect(jsonPath("$.lastOrderTime").value("21:30"));
    }

    @Test
    void testCustomerCreation() {
        // given & when & then - 고객 생성 테스트
        assertThat(testCustomer.getId()).isNotNull();
        assertThat(testCustomer.getLoginId()).startsWith("simple_test_");
        assertThat(testCustomer.getName()).isEqualTo("간단 테스트 고객");
        assertThat(testCustomer.getGrade()).isEqualTo(Customer.CustomerGrade.BASIC);
    }

    @Test
    void testMenuCreation() {
        // given & when & then - 메뉴 생성 테스트
        assertThat(testMenu.getId()).isNotNull();
        assertThat(testMenu.getName()).isEqualTo("간단 디너");
        assertThat(testMenu.getPrice()).isEqualTo(20000);
    }

    @Test
    void testServingStyleCreation() {
        // given & when & then - 서빙 스타일 생성 테스트
        assertThat(testServingStyle.getId()).isNotNull();
        assertThat(testServingStyle.getName()).startsWith("기본_");
        assertThat(testServingStyle.getExtraPrice()).isEqualTo(0);
    }

    @Test
    void testCustomerOrderCountIncrement() {
        // given - 초기 주문 횟수 확인
        int initialCount = testCustomer.getOrderCount();
        
        // when - 고객 주문 횟수 증가
        testCustomer.incrementOrderCount();
        
        // then - 업데이트된 고객 정보 검증
        Customer updatedCustomer = customerRepository.save(testCustomer);
        assertThat(updatedCustomer.getOrderCount()).isEqualTo(initialCount + 1);
    }
}