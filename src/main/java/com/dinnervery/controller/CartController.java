package com.dinnervery.controller;

import com.dinnervery.dto.cart.request.CartAddItemRequest;
import com.dinnervery.dto.cart.request.CartOptionQuantityChangeRequest;
import com.dinnervery.security.SecurityUtils;
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
        SecurityUtils.validateCustomerAccess(customerId);
        Map<String, Object> response = cartService.addItemToCart(customerId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/cart/{customerId}/items")
    public ResponseEntity<Map<String, Object>> deleteCartItem(
            @PathVariable Long customerId,
            @RequestParam(required = false) Long cartItemId) {
        SecurityUtils.validateCustomerAccess(customerId);
        Long itemId = (cartItemId != null && cartItemId != 0) ? cartItemId : 0L;
        Map<String, Object> response = cartService.deleteCartItem(customerId, itemId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/cart/{customerId}")
    public ResponseEntity<Map<String, Object>> getCart(@PathVariable Long customerId) {
        SecurityUtils.validateCustomerAccess(customerId);
        Map<String, Object> response = cartService.getCart(customerId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/cart/{customerId}/items/{cartItemId}/options/{optionId}")
    public ResponseEntity<Map<String, Object>> changeOptionQuantity(
            @PathVariable Long customerId, 
            @PathVariable Long cartItemId, 
            @PathVariable Long optionId,
            @RequestBody CartOptionQuantityChangeRequest request) {
        SecurityUtils.validateCustomerAccess(customerId);
        Map<String, Object> response = cartService.changeOptionQuantity(customerId, cartItemId, optionId, request);
        return ResponseEntity.ok(response);
    }
    
}
