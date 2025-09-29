package com.dinnervery.backend.common.aspect;

import com.dinnervery.backend.common.annotation.RequireDuty;
import com.dinnervery.backend.entity.Employee;
import com.dinnervery.backend.repository.EmployeeRepository;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;

@Aspect
@Component
@RequiredArgsConstructor
public class DutyAuthorizationAspect {

    private final EmployeeRepository employeeRepository;

    @Around("@annotation(requireDuty)")
    public Object checkDutyPermission(ProceedingJoinPoint joinPoint, RequireDuty requireDuty) throws Throwable {
        // 실제 구현에서는 JWT 토큰이나 세션에서 직원 ID를 가져와야 함
        // 여기서는 간단히 헤더에서 employeeId를 가져오는 것으로 가정
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("인증 정보가 없습니다.");
        }

        HttpServletRequest request = attributes.getRequest();
        String employeeIdHeader = request.getHeader("X-Employee-ID");
        
        if (employeeIdHeader == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("직원 ID가 필요합니다.");
        }

        try {
            Long employeeId = Long.parseLong(employeeIdHeader);
            Employee employee = employeeRepository.findById(employeeId)
                    .orElseThrow(() -> new IllegalArgumentException("직원을 찾을 수 없습니다: " + employeeId));

            Employee.EmployeeTask[] requiredTasks = requireDuty.value();
            boolean hasPermission = Arrays.stream(requiredTasks)
                    .anyMatch(task -> employee.getTask() == task);

            if (!hasPermission) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("권한이 없습니다. 필요한 권한: " + Arrays.toString(requiredTasks));
            }

            return joinPoint.proceed();
        } catch (NumberFormatException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("잘못된 직원 ID 형식입니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}
