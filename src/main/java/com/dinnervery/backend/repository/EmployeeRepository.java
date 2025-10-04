package com.dinnervery.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dinnervery.backend.entity.Employee;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    Optional<Employee> findByLoginId(String loginId);
    
    boolean existsByLoginId(String loginId);
    
    List<Employee> findByTask(Employee.EmployeeTask task);
}
