package com.dinnervery.common.aspect;

import com.dinnervery.common.annotation.RequireDuty;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class DutyCheckAspect {

    @Around("@annotation(requireDuty)")
    public Object checkDuty(ProceedingJoinPoint joinPoint, RequireDuty requireDuty) throws Throwable {
        // 실제 구현에서는 현재 로그인한 직원의 정보를 가져와야 합니다
        // 여기서는 간단한 예시로 구현합니다
        
        log.info("Duty check for method: {}", joinPoint.getSignature().getName());
        log.info("Required duties: {}", (Object) requireDuty.value());
        
        // TODO: 실제 구현에서는 다음과 같이 해야 합니다:
        // 1. SecurityContext에서 현재 인증된 사용자 정보 가져오기
        // 2. 사용자가 직원인지 확인
        // 3. 직원의 작업(task)이 요구되는 작업과 일치하는지 확인
        // 4. 권한이 없으면 예외 발생
        
        // 현재는 모든 요청을 허용하도록 구현
        return joinPoint.proceed();
    }
}
