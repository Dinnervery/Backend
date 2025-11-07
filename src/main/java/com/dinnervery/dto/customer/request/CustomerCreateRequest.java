package com.dinnervery.dto.customer.request;

import jakarta.validation.constraints.NotBlank;
 
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerCreateRequest {

    @NotBlank(message = "로그인 ID는 필수입니다.")
    
    private String loginId;

    @NotBlank(message = "비밀번호는 필수입니다.")
    
    private String password;

    @NotBlank(message = "이름은 필수입니다.")
    
    private String name;

    @NotBlank(message = "전화번호는 필수입니다.")
    
    private String phoneNumber;

    @NotBlank(message = "주소는 필수입니다.")
    private String address;

}

