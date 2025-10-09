package com.dinnervery.controller;

import com.dinnervery.dto.response.BusinessHoursResponse;
import com.dinnervery.service.BusinessHoursService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/business-hours")
@RequiredArgsConstructor
public class BusinessHoursController {

    private final BusinessHoursService businessHoursService;

    @GetMapping("/status")
    public ResponseEntity<BusinessHoursResponse> getBusinessHoursStatus() {
        BusinessHoursService.BusinessHoursStatus status = businessHoursService.getBusinessHoursStatus();
        
        BusinessHoursResponse response = new BusinessHoursResponse(
                status.isOpen(),
                status.getOpenTime().toString(),
                status.getCloseTime().toString(),
                status.getLastOrderTime().toString(),
                status.getMessage()
        );
        
        return ResponseEntity.ok(response);
    }
}
