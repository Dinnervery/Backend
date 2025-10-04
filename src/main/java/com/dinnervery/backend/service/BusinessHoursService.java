package com.dinnervery.backend.service;

import org.springframework.stereotype.Service;

import java.time.LocalTime;

@Service
public class BusinessHoursService {

    // 영업시간 설정 (예: 오후 5시 ~ 오후 11시)
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
