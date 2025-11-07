package com.dinnervery.dto.auth.request;

import jakarta.validation.constraints.NotBlank;
 
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignupRequest {

    @NotBlank(message = "로그인 ID는 필수입니다.")
    
    private String loginId;

    
    private String password;

    @NotBlank(message = "이름은 필수입니다.")
    private String name;

    
    private String phoneNumber;

    @NotBlank(message = "주소는 필수입니다.")
    private String address;
}

