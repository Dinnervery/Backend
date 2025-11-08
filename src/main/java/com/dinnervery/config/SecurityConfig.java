package com.dinnervery.config;

import com.dinnervery.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 1. CSRF, Form Login, HTTP Basic 등 불필요한 기능 비활성화
            .csrf(csrf -> csrf.disable())
            .formLogin(formLogin -> formLogin.disable())
            .httpBasic(httpBasic -> httpBasic.disable())
            
            // 2. H2 콘솔의 <iframe>을 허용하도록 설정
            .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.disable()))
            
            // 3. 세션을 사용하지 않음 (토큰 방식은 Stateless)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // 4. API 경로별 권한 설정
            .authorizeHttpRequests(auth -> auth
                // (허용할 API 목록)
                .requestMatchers(
                    "/api/auth/**",              // 로그인, 회원가입
                    "/api/menus/**",             // 메뉴 조회
                    "/api/styles",               // 스타일 조회
                    "/h2-console/**"             // H2 콘솔 (개발용)
                ).permitAll()
                
                // (인증이 필요한 API 목록)
                .requestMatchers(
                    "/api/cart/**",              // 장바구니
                    "/api/orders/**",            // 주문 관리
                    "/api/payments/**",          // 결제
                    "/api/storage"               // 재고 조회
                ).authenticated() // ⬅️ "인증된(로그인한) 사용자만" 허용
                
                // (요리직원 전용 API)
                .requestMatchers(
                    "/api/orders/cooking"        // 요리 목록
                ).hasRole("COOK") // ⬅️ "COOK" 권한이 있어야만 허용
                
                // (배달직원 전용 API)
                .requestMatchers(
                    "/api/orders/delivery"       // 배달 목록
                ).hasRole("DELIVERY") // ⬅️ "DELIVERY" 권한이 있어야만 허용
                
                // 그 외 모든 요청도 인증 필요
                .anyRequest().authenticated()
            )
            
            // 5. 우리가 만든 JWT 필터를 Spring Security 필터 체인에 추가
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}

