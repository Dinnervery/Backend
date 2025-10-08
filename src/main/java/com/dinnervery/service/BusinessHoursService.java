package com.dinnervery.service;

import org.springframework.stereotype.Service;

import java.time.LocalTime;

@Service
public class BusinessHoursService {

    // 영업시간 설정 (오후 3시 30분~ 오후 10시)
    private static final LocalTime OPEN_TIME = LocalTime.of(15, 30); // 오후 3시 30분
    private static final LocalTime CLOSE_TIME = LocalTime.of(22, 0); // 오후 10시
    private static final LocalTime LAST_ORDER_TIME = LocalTime.of(21, 30); // 오후 9시 30분

    /**
     * 현재 시간이 영업시간인지 확인
     * @return 영업시간이면 true, 아니면 false
     */
    public boolean isBusinessHours() {
        LocalTime now = LocalTime.now();
        
        return !now.isBefore(OPEN_TIME) && now.isBefore(CLOSE_TIME);
    }

    /**
     * 현재 시간이 마감시간 이후인지 확인
     * @return 마감시간 이후면 true, 아니면 false
     */
    public boolean isAfterLastOrderTime() {
        LocalTime now = LocalTime.now();
        return now.isAfter(LAST_ORDER_TIME);
    }

    /**
     * 배송 희망 시간이 유효한지 확인
     * @param deliveryTime 배송 희망 시간
     * @return 유효하면 true, 아니면 false
     */
    public boolean isValidDeliveryTime(LocalTime deliveryTime) {
        LocalTime now = LocalTime.now();
        LocalTime minDeliveryTime = now.plusMinutes(30);
        
        // 배송 가능 시간: 16:00 ~ 22:00
        LocalTime deliveryStartTime = LocalTime.of(16, 0);
        LocalTime deliveryEndTime = LocalTime.of(22, 0);
        
        // 최소 배송 시간과 배송 가능 시간 중 더 늦은 시간으로 설정
        LocalTime effectiveMinTime = minDeliveryTime.isAfter(deliveryStartTime) ? minDeliveryTime : deliveryStartTime;
        
        // 10분 단위로 반올림
        int minutes = effectiveMinTime.getMinute();
        int roundedMinutes = ((minutes + 9) / 10) * 10;
        if (roundedMinutes >= 60) {
            effectiveMinTime = effectiveMinTime.plusHours(1).withMinute(0);
        } else {
            effectiveMinTime = effectiveMinTime.withMinute(roundedMinutes);
        }
        
        return !deliveryTime.isBefore(effectiveMinTime) && 
               !deliveryTime.isAfter(deliveryEndTime) &&
               deliveryTime.getMinute() % 10 == 0;
    }

    /**
     * 영업시간 상태 정보 반환
     * @return 영업시간 상태 정보
     */
    public BusinessHoursStatus getBusinessHoursStatus() {
        boolean isOpen = isBusinessHours();
        
        return BusinessHoursStatus.builder()
                .isOpen(isOpen)
                .openTime(OPEN_TIME)
                .closeTime(CLOSE_TIME)
                .lastOrderTime(LAST_ORDER_TIME)
                .message(isOpen ? "현재 주문 가능합니다" : "영업시간이 아닙니다")
                .build();
    }

    /**
     * 영업시간 상태 정보를 담는 내부 클래스
     */
    public static class BusinessHoursStatus {
        private boolean isOpen;
        private LocalTime openTime;
        private LocalTime closeTime;
        private LocalTime lastOrderTime;
        private String message;

        public static BusinessHoursStatusBuilder builder() {
            return new BusinessHoursStatusBuilder();
        }

        public static class BusinessHoursStatusBuilder {
            private boolean isOpen;
            private LocalTime openTime;
            private LocalTime closeTime;
            private LocalTime lastOrderTime;
            private String message;

            public BusinessHoursStatusBuilder isOpen(boolean isOpen) {
                this.isOpen = isOpen;
                return this;
            }

            public BusinessHoursStatusBuilder openTime(LocalTime openTime) {
                this.openTime = openTime;
                return this;
            }

            public BusinessHoursStatusBuilder closeTime(LocalTime closeTime) {
                this.closeTime = closeTime;
                return this;
            }

            public BusinessHoursStatusBuilder lastOrderTime(LocalTime lastOrderTime) {
                this.lastOrderTime = lastOrderTime;
                return this;
            }

            public BusinessHoursStatusBuilder message(String message) {
                this.message = message;
                return this;
            }

            public BusinessHoursStatus build() {
                BusinessHoursStatus status = new BusinessHoursStatus();
                status.isOpen = this.isOpen;
                status.openTime = this.openTime;
                status.closeTime = this.closeTime;
                status.lastOrderTime = this.lastOrderTime;
                status.message = this.message;
                return status;
            }
        }

        // Getters
        public boolean isOpen() { return isOpen; }
        public LocalTime getOpenTime() { return openTime; }
        public LocalTime getCloseTime() { return closeTime; }
        public LocalTime getLastOrderTime() { return lastOrderTime; }
        public String getMessage() { return message; }
    }
}
