package com.dinnervery.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dinnervery.entity.Style;

import java.util.Optional;

public interface StyleRepository extends JpaRepository<Style, Long> {
    
    Optional<Style> findByName(String name);
}

