package com.dinnervery.service;

import com.dinnervery.entity.MenuOption;
import com.dinnervery.entity.Storage;
import com.dinnervery.repository.StorageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StorageService {

	private final StorageRepository storageRepository;

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
		if (storage == null) return; // 스토리지 미연결 옵션은 재고 체크 제외
		int required = option.getStorageConsumption() * quantity;
		if (storage.getQuantity() < required) {
			throw new IllegalStateException(storage.getName() + " 재고가 부족합니다.");
		}
	}

	@Transactional
	public void deductStock(MenuOption option, int quantity) {
		Storage storage = option.getStorageItem();
		if (storage == null) return; // 스토리지 미연결 옵션은 차감 제외
		int consumed = option.getStorageConsumption() * quantity;
		storage.setQuantity(storage.getQuantity() - consumed);
		storageRepository.save(storage);
	}
}


