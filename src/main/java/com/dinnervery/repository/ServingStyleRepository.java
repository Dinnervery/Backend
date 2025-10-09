package com.dinnervery.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dinnervery.entity.ServingStyle;

import java.util.List;
import java.util.Optional;

public interface ServingStyleRepository extends JpaRepository<ServingStyle, Long> {

    List<ServingStyle> findByIsActiveTrue();
    
    Optional<ServingStyle> findByName(String name);
}


