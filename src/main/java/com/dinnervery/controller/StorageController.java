package com.dinnervery.controller;

import com.dinnervery.entity.MenuOption;
import com.dinnervery.repository.MenuOptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class StorageController {

	private final MenuOptionRepository menuOptionRepository;

	@GetMapping("/storage")
	public ResponseEntity<Map<String, Object>> getAllStorage() {
		// storageItem이 연결된 MenuOption만 조회
		List<MenuOption> menuOptions = menuOptionRepository.findAll().stream()
				.filter(option -> option.getStorageItem() != null)
				.collect(Collectors.toList());

		List<Map<String, Object>> items = menuOptions.stream().map(option -> {
			Map<String, Object> m = new HashMap<>();
			m.put("optionId", option.getId());
			m.put("optionName", option.getName());
			m.put("quantity", option.getStorageItem().getQuantity());
			return m;
		}).collect(Collectors.toList());

		Map<String, Object> resp = new HashMap<>();
		resp.put("storageItems", items);
		return ResponseEntity.ok(resp);
	}
}


