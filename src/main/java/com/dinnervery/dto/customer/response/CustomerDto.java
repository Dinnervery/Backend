package com.dinnervery.dto.customer.response;

import com.dinnervery.entity.Customer;
import jakarta.validation.constraints.NotBlank;
 
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDto {

    private Long id;

    @NotBlank(message = "로그인 ID는 필수입니다.")
    
    private String loginId;

    @NotBlank(message = "비밀번호는 필수입니다.")
    
    private String password;

    @NotBlank(message = "이름은 필수입니다.")
    
    private String name;

    @NotBlank(message = "전화번호는 필수입니다.")
    
    private String phoneNumber;

    private Integer orderCount;
    private Customer.CustomerGrade grade;

    public static CustomerDto from(Customer customer) {
        return CustomerDto.builder()
                .id(customer.getId())
                .loginId(customer.getLoginId())
                .name(customer.getName())
                .phoneNumber(customer.getPhoneNumber())
                .orderCount(customer.getOrderCount())
                .grade(customer.getGrade())
                .build();
    }
}

