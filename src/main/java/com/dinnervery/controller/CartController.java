package com.dinnervery.controller;

import com.dinnervery.dto.cart.request.CartAddItemRequest;
import com.dinnervery.dto.cart.request.CartOptionQuantityChangeRequest;
import com.dinnervery.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @PostMapping("/cart/{customerId}/items")
    public ResponseEntity<Map<String, Object>> addItemToCart(@PathVariable Long customerId, @RequestBody CartAddItemRequest request) {
        Map<String, Object> response = cartService.addItemToCart(customerId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/cart/{customerId}")
    public ResponseEntity<Map<String, Object>> getCart(@PathVariable Long customerId) {
        Map<String, Object> response = cartService.getCart(customerId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/cart/{customerId}/items/{cartItemId}/options/{optionId}")
    public ResponseEntity<Map<String, Object>> changeOptionQuantity(
            @PathVariable Long customerId, 
            @PathVariable Long cartItemId, 
            @PathVariable Long optionId,
            @RequestBody CartOptionQuantityChangeRequest request) {
        Map<String, Object> response = cartService.changeOptionQuantity(customerId, cartItemId, optionId, request);
        return ResponseEntity.ok(response);
    }
    
}
