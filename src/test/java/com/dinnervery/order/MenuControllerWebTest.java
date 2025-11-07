package com.dinnervery.order;

import com.dinnervery.controller.MenuController;
import com.dinnervery.entity.Customer;
import com.dinnervery.entity.Menu;
import com.dinnervery.entity.MenuOption;
import com.dinnervery.entity.Style;
import com.dinnervery.repository.CustomerRepository;
import com.dinnervery.repository.MenuOptionRepository;
import com.dinnervery.repository.MenuRepository;
import com.dinnervery.repository.StyleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MenuController.class)
@ActiveProfiles("test")
class MenuControllerWebTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean private MenuRepository menuRepository;
    @MockitoBean private MenuOptionRepository menuOptionRepository;
    @MockitoBean private StyleRepository styleRepository;
    @MockitoBean private CustomerRepository customerRepository;

    private Menu menu;
    private MenuOption option;
    private Style style;
    private Customer customer;

    @BeforeEach
    void setUp() {
        menu = Menu.builder().name("테스트 메뉴").price(10000).build();
        option = MenuOption.builder().menu(menu).name("옵션1").price(2000).defaultQty(1).build();
        style = Style.builder().name("기본").extraPrice(0).build();
        customer = Customer.builder().loginId("user").password("pw").name("고객").phoneNumber("010-0000-0000").build();

        when(menuRepository.findAll()).thenReturn(List.of(menu));
        when(menuOptionRepository.findByMenu_Id(anyLong())).thenReturn(List.of(option));
        when(styleRepository.findAll()).thenReturn(List.of(style));
        when(customerRepository.findById(anyLong())).thenReturn(Optional.of(customer));
    }

    @Test
    void getAllMenus_returnsOk() throws Exception {
        mockMvc.perform(get("/api/menus").param("customerId", "1").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.menus").exists());
    }


    @Test
    void getMenuOptions_returnsOk() throws Exception {
        mockMvc.perform(get("/api/menus/{menuId}/options", 1L).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.options").exists());
    }

    @Test
    void getStyles_returnsOk() throws Exception {
        mockMvc.perform(get("/api/styles").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.styles").exists());
    }
}


