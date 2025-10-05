package com.dinnervery.backend.service;

import com.dinnervery.backend.entity.Employee;
import com.dinnervery.backend.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EmployeeAvailabilityService {

    private final EmployeeRepository employeeRepository;

    /**
     * 가용한 조리사가 있는지 확인
     * @return 가용한 조리사가 있으면 true, 없으면 false
     */
    public boolean hasAvailableCook() {
        List<Employee> availableCooks = employeeRepository.findByTaskAndWorkStatus(
                Employee.EmployeeTask.COOK, 
                Employee.WorkStatus.AVAILABLE
        );
        return !availableCooks.isEmpty();
    }

    /**
     * 가용한 배달원이 있는지 확인
     * @return 가용한 배달원이 있으면 true, 없으면 false
     */
    public boolean hasAvailableDeliveryPerson() {
        List<Employee> availableDeliveryPersons = employeeRepository.findByTaskAndWorkStatus(
                Employee.EmployeeTask.DELIVERY, 
                Employee.WorkStatus.AVAILABLE
        );
        return !availableDeliveryPersons.isEmpty();
    }

    /**
     * 가용한 조리사 중 한 명을 할당
     * @return 할당된 조리사
     */
    public Optional<Employee> assignAvailableCook() {
        List<Employee> availableCooks = employeeRepository.findByTaskAndWorkStatus(
                Employee.EmployeeTask.COOK, 
                Employee.WorkStatus.AVAILABLE
        );
        
        if (availableCooks.isEmpty()) {
            return Optional.empty();
        }
        
        Employee assignedCook = availableCooks.get(0);
        assignedCook.startWork();
        employeeRepository.save(assignedCook);
        
        return Optional.of(assignedCook);
    }

    /**
     * 가용한 배달원 중 한 명을 할당
     * @return 할당된 배달원
     */
    public Optional<Employee> assignAvailableDeliveryPerson() {
        List<Employee> availableDeliveryPersons = employeeRepository.findByTaskAndWorkStatus(
                Employee.EmployeeTask.DELIVERY, 
                Employee.WorkStatus.AVAILABLE
        );
        
        if (availableDeliveryPersons.isEmpty()) {
            return Optional.empty();
        }
        
        Employee assignedDeliveryPerson = availableDeliveryPersons.get(0);
        assignedDeliveryPerson.startWork();
        employeeRepository.save(assignedDeliveryPerson);
        
        return Optional.of(assignedDeliveryPerson);
    }

    /**
     * 직원의 작업 완료 처리
     * @param employee 작업을 완료한 직원
     */
    public void releaseEmployee(Employee employee) {
        employee.finishWork();
        employeeRepository.save(employee);
    }
}
