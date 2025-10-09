package com.dinnervery.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dinnervery.entity.Employee;

import java.util.List;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    Optional<Employee> findByLoginId(String loginId);
    
    boolean existsByLoginId(String loginId);
    
    List<Employee> findByTask(Employee.EmployeeTask task);
    
    List<Employee> findByTaskAndWorkStatus(Employee.EmployeeTask task, Employee.WorkStatus workStatus);
}
