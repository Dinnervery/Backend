package com.dinnervery.controller;

import com.dinnervery.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class StorageController {

	private final StorageService storageService;

	@GetMapping("/storage")
	public ResponseEntity<Map<String, Object>> getAllStorage() {
		Map<String, Object> response = storageService.getAllStorage();
		return ResponseEntity.ok(response);
	}
}


