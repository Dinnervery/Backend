package com.dinnervery.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalTime;

@Service
public class BusinessHoursService {

    @Value("${business.hours.open-time}")
    private String openTimeStr;
    
    @Value("${business.hours.close-time}")
    private String closeTimeStr;
    
    @Value("${business.hours.last-order-time}")
    private String lastOrderTimeStr;
    
    @Value("${business.hours.delivery-start-time}")
    private String deliveryStartTimeStr;
    
    @Value("${business.hours.delivery-end-time}")
    private String deliveryEndTimeStr;
    
    @Value("${business.hours.min-delivery-minutes}")
    private int minDeliveryMinutes;
    
    @Value("${business.hours.delivery-time-unit}")
    private int deliveryTimeUnit;

    private LocalTime getOpenTime() {
        return LocalTime.parse(openTimeStr);
    }
    
    private LocalTime getCloseTime() {
        return LocalTime.parse(closeTimeStr);
    }
    
    private LocalTime getLastOrderTime() {
        return LocalTime.parse(lastOrderTimeStr);
    }
    
    private LocalTime getDeliveryStartTime() {
        return LocalTime.parse(deliveryStartTimeStr);
    }
    
    private LocalTime getDeliveryEndTime() {
        return LocalTime.parse(deliveryEndTimeStr);
    }

    /**
     * 현재 시간이 영업시간인지 확인
     * @return 영업시간이면 true, 아니면 false
     */
    public boolean isBusinessHours() {
        LocalTime now = LocalTime.now();
        
        return !now.isBefore(getOpenTime()) && now.isBefore(getCloseTime());
    }

    /**
     * 현재 시간이 마감시간 이후인지 확인
     * @return 마감시간 이후면 true, 아니면 false
     */
    public boolean isAfterLastOrderTime() {
        LocalTime now = LocalTime.now();
        return now.isAfter(getLastOrderTime());
    }

    /**
     * 배송 희망 시간이 유효한지 확인
     * @param deliveryTime 배송 희망 시간
     * @return 유효하면 true, 아니면 false
     */
    public boolean isValidDeliveryTime(LocalTime deliveryTime) {
        LocalTime now = LocalTime.now();
        LocalTime minDeliveryTime = now.plusMinutes(minDeliveryMinutes);
        
        // 최소 배송 시간과 배송 가능 시간 중 더 늦은 시간으로 설정
        LocalTime effectiveMinTime = minDeliveryTime.isAfter(getDeliveryStartTime()) ? minDeliveryTime : getDeliveryStartTime();
        
        // 배송 시간 단위로 반올림
        int minutes = effectiveMinTime.getMinute();
        int roundedMinutes = ((minutes + deliveryTimeUnit - 1) / deliveryTimeUnit) * deliveryTimeUnit;
        if (roundedMinutes >= 60) {
            effectiveMinTime = effectiveMinTime.plusHours(1).withMinute(0);
        } else {
            effectiveMinTime = effectiveMinTime.withMinute(roundedMinutes);
        }
        
        return !deliveryTime.isBefore(effectiveMinTime) && 
               !deliveryTime.isAfter(getDeliveryEndTime()) &&
               deliveryTime.getMinute() % deliveryTimeUnit == 0;
    }

    /**
     * 영업시간 상태 정보 반환
     * @return 영업시간 상태 정보
     */
    public BusinessHoursStatus getBusinessHoursStatus() {
        boolean isOpen = isBusinessHours();
        
        return BusinessHoursStatus.builder()
                .isOpen(isOpen)
                .openTime(getOpenTime())
                .closeTime(getCloseTime())
                .lastOrderTime(getLastOrderTime())
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
