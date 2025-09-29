package com.dinnervery.backend.controller;

import com.dinnervery.backend.dto.cart.CartAddItemRequest;
import com.dinnervery.backend.entity.Cart;
import com.dinnervery.backend.entity.CartItem;
import com.dinnervery.backend.entity.Customer;
import com.dinnervery.backend.repository.CartItemRepository;
import com.dinnervery.backend.repository.CartRepository;
import com.dinnervery.backend.repository.CustomerRepository;
import com.dinnervery.backend.entity.Menu;
import com.dinnervery.backend.entity.ServingStyle;
import com.dinnervery.backend.repository.MenuRepository;
import com.dinnervery.backend.repository.ServingStyleRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CartController {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final CustomerRepository customerRepository;
    private final MenuRepository menuRepository;
    private final ServingStyleRepository servingStyleRepository;

    @PostMapping("/carts/{customerId}/items")
    public ResponseEntity<Map<String, Object>> addItemToCart(@PathVariable Long customerId, @RequestBody CartAddItemRequest request) {
        // 고객 조회
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("고객을 찾을 수 없습니다: " + customerId));

        // 메뉴 조회
        Menu menu = menuRepository.findById(request.getMenuId())
                .orElseThrow(() -> new IllegalArgumentException("메뉴를 찾을 수 없습니다: " + request.getMenuId()));

        // 서빙 스타일 조회
        ServingStyle servingStyle = servingStyleRepository.findById(request.getServingStyleId())
                .orElseThrow(() -> new IllegalArgumentException("서빙 스타일을 찾을 수 없습니다: " + request.getServingStyleId()));

        // 장바구니 조회 또는 생성
        Optional<Cart> existingCart = cartRepository.findByCustomer_Id(customerId);
        Cart cart = existingCart.orElseGet(() -> {
            Cart newCart = Cart.builder().customer(customer).build();
            return cartRepository.save(newCart);
        });

        // 장바구니 아이템 생성
        CartItem cartItem = CartItem.builder()
                .menu(menu)
                .servingStyle(servingStyle)
                .quantity(request.getQuantity())
                .build();

        cart.addCartItem(cartItem);
        cartRepository.save(cart);

        Map<String, Object> response = new HashMap<>();
        response.put("cartItemId", cartItem.getId());
        response.put("message", "장바구니에 아이템이 추가되었습니다");

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/carts/{customerId}/items")
    public ResponseEntity<List<Map<String, Object>>> getCartItems(@PathVariable Long customerId) {
        Optional<Cart> cart = cartRepository.findByCustomer_Id(customerId);
        
        if (cart.isEmpty()) {
            return ResponseEntity.ok(List.of());
        }

        List<CartItem> cartItems = cartItemRepository.findByCart_Id(cart.get().getId());

        List<Map<String, Object>> response = cartItems.stream()
                .map(item -> {
                    Map<String, Object> itemMap = new HashMap<>();
                    itemMap.put("cartItemId", item.getId());
                    itemMap.put("menuId", item.getMenu().getId());
                    itemMap.put("menuName", item.getMenu().getName());
                    itemMap.put("servingStyleId", item.getServingStyle().getId());
                    itemMap.put("servingStyleName", item.getServingStyle().getName());
                    itemMap.put("quantity", item.getQuantity());
                    itemMap.put("createdAt", item.getCreatedAt());
                    return itemMap;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/carts/{customerId}/items/{cartItemId}")
    public ResponseEntity<Map<String, Object>> removeCartItem(@PathVariable Long customerId, @PathVariable Long cartItemId) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new IllegalArgumentException("장바구니 아이템을 찾을 수 없습니다: " + cartItemId));

        cartItemRepository.delete(cartItem);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "장바구니 아이템이 삭제되었습니다");

        return ResponseEntity.ok(response);
    }
}
