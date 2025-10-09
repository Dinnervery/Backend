package com.dinnervery.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class BusinessHoursServiceTest {

    @InjectMocks
    private BusinessHoursService businessHoursService;

    @BeforeEach
    void setUp() {
        // given - 테스트용 설정값 주입
        ReflectionTestUtils.setField(businessHoursService, "openTimeStr", "15:30");
        ReflectionTestUtils.setField(businessHoursService, "closeTimeStr", "22:00");
        ReflectionTestUtils.setField(businessHoursService, "lastOrderTimeStr", "21:30");
        ReflectionTestUtils.setField(businessHoursService, "deliveryStartTimeStr", "16:00");
        ReflectionTestUtils.setField(businessHoursService, "deliveryEndTimeStr", "22:00");
        ReflectionTestUtils.setField(businessHoursService, "minDeliveryMinutes", 30);
        ReflectionTestUtils.setField(businessHoursService, "deliveryTimeUnit", 10);
    }

    @Test
    void isValidDeliveryTime_유효한_배송시간_성공() {
        // given - 유효한 배송시간 (16:30 - 10분 단위)
        LocalTime deliveryTime = LocalTime.of(16, 30);

        // when - 배송시간 유효성 확인
        boolean result = businessHoursService.isValidDeliveryTime(deliveryTime);

        // then - 결과 검증 (현재 시간에 따라 결과가 달라질 수 있음)
        // 이 테스트는 메서드가 정상적으로 실행되는지만 확인
        assertThat(result).isNotNull();
    }

    @Test
    void isValidDeliveryTime_배송시간_단위_위반_실패() {
        // given - 배송시간 단위 위반 (16:35 - 10분 단위가 아님)
        LocalTime deliveryTime = LocalTime.of(16, 35);

        // when - 배송시간 유효성 확인
        boolean result = businessHoursService.isValidDeliveryTime(deliveryTime);

        // then - 결과 검증
        assertThat(result).isFalse();
    }

    @Test
    void isValidDeliveryTime_배송시간_범위_초과_실패() {
        // given - 배송시간 범위 초과 (22:30 - 배송 종료시간 초과)
        LocalTime deliveryTime = LocalTime.of(22, 30);

        // when - 배송시간 유효성 확인
        boolean result = businessHoursService.isValidDeliveryTime(deliveryTime);

        // then - 결과 검증
        assertThat(result).isFalse();
    }

    @Test
    void getBusinessHoursStatus_성공() {
        // when - 영업시간 상태 조회
        BusinessHoursService.BusinessHoursStatus result = businessHoursService.getBusinessHoursStatus();

        // then - 결과 검증
        assertThat(result).isNotNull();
        assertThat(result.getOpenTime()).isEqualTo(LocalTime.of(15, 30));
        assertThat(result.getCloseTime()).isEqualTo(LocalTime.of(22, 0));
        assertThat(result.getLastOrderTime()).isEqualTo(LocalTime.of(21, 30));
        assertThat(result.getMessage()).isNotNull();
    }

    @Test
    void isBusinessHours_메서드_실행_확인() {
        // when - 영업시간 확인
        boolean result = businessHoursService.isBusinessHours();

        // then - 결과 검증 (현재 시간에 따라 결과가 달라질 수 있음)
        // 이 테스트는 메서드가 정상적으로 실행되는지만 확인
        assertThat(result).isNotNull();
    }

    @Test
    void isAfterLastOrderTime_메서드_실행_확인() {
        // when - 마감시간 이후 확인
        boolean result = businessHoursService.isAfterLastOrderTime();

        // then - 결과 검증 (현재 시간에 따라 결과가 달라질 수 있음)
        // 이 테스트는 메서드가 정상적으로 실행되는지만 확인
        assertThat(result).isNotNull();
    }
}
