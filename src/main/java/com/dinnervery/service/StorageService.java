package com.dinnervery.service;

import com.dinnervery.entity.MenuOption;
import com.dinnervery.entity.Storage;
import com.dinnervery.repository.MenuOptionRepository;
import com.dinnervery.repository.StorageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StorageService {

	private final StorageRepository storageRepository;
	private final MenuOptionRepository menuOptionRepository;

	@Scheduled(cron = "0 0 5 * * *")
	@Transactional
	public void resetDailyStock() {
		List<Storage> all = storageRepository.findAll();
		for (Storage s : all) {
			s.setQuantity(100);
		}
		storageRepository.saveAll(all);
	}

	@Transactional(readOnly = true)
	public void checkStock(MenuOption option, int quantity) {
		Storage storage = option.getStorageItem();
		if (storage == null) return;
		int required = option.getStorageConsumption() * quantity;
		if (storage.getQuantity() < required) {
			throw new IllegalStateException(storage.getName() + " 재고가 부족합니다.");
		}
	}

	@Transactional
	public void deductStock(MenuOption option, int quantity) {
		Storage storage = option.getStorageItem();
		if (storage == null) return;
		int consumed = option.getStorageConsumption() * quantity;
		storage.setQuantity(storage.getQuantity() - consumed);
		storageRepository.save(storage);
	}

	public Map<String, Object> getAllStorage() {
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
		return resp;
	}
}
