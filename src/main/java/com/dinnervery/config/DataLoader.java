package com.dinnervery.config;

import com.dinnervery.repository.StorageRepository;
import com.dinnervery.repository.StaffRepository;
import com.dinnervery.entity.Storage;
import com.dinnervery.entity.Staff;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@SuppressWarnings("null")
public class DataLoader implements CommandLineRunner {

    private final StorageRepository storageRepository;
    private final StaffRepository staffRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        createStorages();
        createStaff();
        
        System.out.println("초기 데이터 로딩이 완료되었습니다.");
    }

    private void createStorages() {
        if (storageRepository.count() == 0) {
            Storage meat = Storage.builder().name("고기").quantity(100).build();
            Storage wine = Storage.builder().name("와인").quantity(100).build();
            Storage veggie = Storage.builder().name("채소").quantity(100).build();
            Storage coffee = Storage.builder().name("커피").quantity(100).build();
            Storage champagne = Storage.builder().name("샴페인").quantity(100).build();
            Storage baguette = Storage.builder().name("바게트빵").quantity(100).build();
            Storage egg = Storage.builder().name("계란").quantity(100).build();
            storageRepository.save(meat);
            storageRepository.save(wine);
            storageRepository.save(veggie);
            storageRepository.save(coffee);
            storageRepository.save(champagne);
            storageRepository.save(baguette);
            storageRepository.save(egg);
        }
    }

    private void createStaff() {
        if (staffRepository.count() == 0) {
            Staff cook = Staff.builder()
                    .loginId("cook")
                    .password(passwordEncoder.encode("cook123"))
                    .name("요리사")
                    .phoneNumber("010-1111-1111")
                    .task(Staff.StaffTask.COOK)
                    .build();
            staffRepository.save(cook);

            Staff deliveryPerson = Staff.builder()
                    .loginId("delivery")
                    .password(passwordEncoder.encode("delivery123"))
                    .name("배달원")
                    .phoneNumber("010-2222-2222")
                    .task(Staff.StaffTask.DELIVERY)
                    .build();
            staffRepository.save(deliveryPerson);
        }
    }
}
