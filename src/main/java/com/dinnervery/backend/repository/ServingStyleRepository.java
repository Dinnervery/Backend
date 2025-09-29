package com.dinnervery.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dinnervery.backend.entity.ServingStyle;

import java.util.List;

@Repository
public interface ServingStyleRepository extends JpaRepository<ServingStyle, Long> {

    List<ServingStyle> findByIsActiveTrue();
    
    ServingStyle findByName(String name);
}


