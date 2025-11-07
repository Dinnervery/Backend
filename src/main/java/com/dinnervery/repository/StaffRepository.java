package com.dinnervery.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dinnervery.entity.Staff;

import java.util.List;
import java.util.Optional;

public interface StaffRepository extends JpaRepository<Staff, Long> {

	Optional<Staff> findByLoginId(String loginId);

	boolean existsByLoginId(String loginId);

	List<Staff> findByTask(Staff.StaffTask task);
}



