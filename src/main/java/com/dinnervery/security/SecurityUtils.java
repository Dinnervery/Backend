package com.dinnervery.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {

    private static final String ROLE_PREFIX = "ROLE_";

    /**
     * 현재 인증된 사용자의 ID를 반환
     */
    public static Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new IllegalStateException("인증 정보가 없습니다");
        }
        return (Long) authentication.getPrincipal();
    }

    /**
     * 현재 인증된 사용자의 Role을 반환 (예: "CUSTOMER", "COOK", "DELIVERY")
     */
    public static String getCurrentUserRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getAuthorities() == null || authentication.getAuthorities().isEmpty()) {
            throw new IllegalStateException("인증 정보가 없습니다");
        }
        
        String authority = authentication.getAuthorities().iterator().next().getAuthority();
        // "ROLE_CUSTOMER" -> "CUSTOMER"
        if (authority.startsWith(ROLE_PREFIX)) {
            return authority.substring(ROLE_PREFIX.length());
        }
        return authority;
    }

    /**
     * 고객 전용 API 접근 권한 검증
     * @param customerId 경로에서 받은 customerId
     * @throws IllegalArgumentException role이 CUSTOMER가 아니거나, userId와 customerId가 일치하지 않는 경우
     */
    public static void validateCustomerAccess(Long customerId) {
        Long userId = getCurrentUserId();
        String role = getCurrentUserRole();

        if (!"CUSTOMER".equals(role)) {
            throw new IllegalArgumentException("고객만 접근 가능합니다");
        }

        if (!userId.equals(customerId)) {
            throw new IllegalArgumentException("접근 권한이 없습니다");
        }
    }
}

