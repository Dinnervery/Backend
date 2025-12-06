package com.dinnervery.service;

import com.dinnervery.entity.OrderItemOption;
import com.dinnervery.entity.Storage;
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

	/**
	 * 옵션 이름을 재고 이름으로 변환
	 * 백엔드에서 옵션 이름과 재고 이름의 매핑을 관리
	 */
	public String getStorageName(String optionName) {
		return switch (optionName) {
			case "에그 스크램블" -> "계란";
			case "바게트빵" -> "바게트빵";
			case "베이컨" -> "고기";
			case "샴페인" -> "샴페인";
			case "스테이크" -> "고기";
			case "와인" -> "와인";
			case "샐러드" -> "채소";
			case "커피" -> "커피";
			case "커피포트" -> "커피";
			default -> optionName;  // 매핑이 없으면 옵션 이름 그대로 사용
		};
	}

	/**
	 * 옵션 이름에 따른 재고 소비량을 반환
	 * 백엔드에서 옵션별 재고 소비량을 관리
	 */
	public int getStorageConsumption(String optionName) {
		return switch (optionName) {
			case "에그 스크램블" -> 1;
			case "바게트빵" -> 1;
			case "베이컨" -> 1;
			case "샴페인" -> 1;
			case "스테이크" -> 1;
			case "와인" -> 1;
			case "샐러드" -> 1;
			case "커피" -> 1;
			case "커피포트" -> 5;
			default -> 1;  // 기본값 1
		};
	}

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
	public void checkStock(OrderItemOption option) {
		// 옵션 이름을 재고 이름으로 변환
		String storageName = getStorageName(option.getOptionName());
		Storage storage = storageRepository.findByName(storageName)
				.orElse(null);
		if (storage == null) {
			return; // 해당 이름의 재고가 없으면 체크하지 않음
		}
		int required = option.getStorageConsumption() * option.getQuantity();
		if (storage.getQuantity() < required) {
			throw new IllegalStateException(storage.getName() + " 재고가 부족합니다.");
		}
	}

	@Transactional
	public void deductStock(OrderItemOption option) {
		// 옵션 이름을 재고 이름으로 변환
		String storageName = getStorageName(option.getOptionName());
		Storage storage = storageRepository.findByName(storageName)
				.orElse(null);
		if (storage == null) {
			return; // 해당 이름의 재고가 없으면 차감하지 않음
		}
		int consumed = option.getStorageConsumption() * option.getQuantity();
		storage.setQuantity(storage.getQuantity() - consumed);
		storageRepository.save(storage);
	}

	public Map<String, Object> getAllStorage() {
		List<Storage> storages = storageRepository.findAll();

		List<Map<String, Object>> items = storages.stream().map(storage -> {
			Map<String, Object> m = new HashMap<>();
			m.put("storageId", storage.getId());
			m.put("name", storage.getName());
			m.put("quantity", storage.getQuantity());
			return m;
		}).collect(Collectors.toList());

		Map<String, Object> resp = new HashMap<>();
		resp.put("storageItems", items);
		return resp;
	}
}
