package com.dinnervery.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dinnervery.entity.Storage;

import java.util.Optional;

public interface StorageRepository extends JpaRepository<Storage, Long> {
	Optional<Storage> findByName(String name);
}


