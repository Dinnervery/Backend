package com.dinnervery.backend.repository;

import com.dinnervery.backend.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    Optional<Task> findByOrderId(Long orderId);
    Optional<Task> findByOrderIdAndStatus(Long orderId, String status);
}
