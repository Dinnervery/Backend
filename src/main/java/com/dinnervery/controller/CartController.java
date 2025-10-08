package com.dinnervery.controller;

import com.dinnervery.dto.request.CartAddItemRequest;
import com.dinnervery.dto.request.CartOptionQuantityChangeRequest;
import com.dinnervery.entity.Cart;
import com.dinnervery.entity.CartItem;
import com.dinnervery.entity.Customer;
import com.dinnervery.entity.Menu;
import com.dinnervery.entity.MenuOption;
import com.dinnervery.entity.ServingStyle;
import com.dinnervery.repository.CartItemRepository;
import com.dinnervery.repository.CartRepository;
import com.dinnervery.repository.CustomerRepository;
import com.dinnervery.repository.MenuRepository;
import com.dinnervery.repository.MenuOptionRepository;
import com.dinnervery.repository.ServingStyleRepository;

import lombok.RequiredArgsConstructor;
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
    private final MenuOptionRepository menuOptionRepository;
    private final ServingStyleRepository servingStyleRepository;

    @PostMapping("/cart/{customerId}/items")
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
                .quantity(request.getMenuQuantity())
                .build();

        cart.addCartItem(cartItem);
        cartRepository.save(cart);

        Map<String, Object> response = new HashMap<>();
        response.put("cartItemId", cartItem.getId());
        response.put("menuId", cartItem.getMenu().getId());
        response.put("menuName", cartItem.getMenu().getName());
        response.put("quantity", cartItem.getQuantity());
        response.put("unitPrice", cartItem.getMenu().getPrice());
        response.put("servingStyleId", cartItem.getServingStyle().getId());
        response.put("servingStyleName", cartItem.getServingStyle().getName());
        response.put("servingStylePrice", cartItem.getServingStyle().getExtraPrice());
        response.put("totalPrice", (cartItem.getMenu().getPrice() + cartItem.getServingStyle().getExtraPrice()) * cartItem.getQuantity());
        response.put("addedAt", cartItem.getCreatedAt() != null ? cartItem.getCreatedAt() : java.time.LocalDateTime.now());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/cart/{customerId}")
    public ResponseEntity<Map<String, Object>> getCart(@PathVariable Long customerId) {
        Optional<Cart> cart = cartRepository.findByCustomer_Id(customerId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("cartId", cart.map(Cart::getId).orElse(null));
        response.put("customerId", customerId);
        
        if (cart.isEmpty()) {
            response.put("dinnerItems", List.of());
            response.put("totalAmount", 0);
            return ResponseEntity.ok(response);
        }

        List<CartItem> cartItems = cartItemRepository.findByCart_Id(cart.get().getId());

        List<Map<String, Object>> cartItemsList = cartItems.stream()
                .map(item -> {
                    Map<String, Object> itemMap = new HashMap<>();
                    itemMap.put("cartItemId", item.getId());
                    
                    // 디너 아이템 정보
                    Map<String, Object> dinnerItem = new HashMap<>();
                    dinnerItem.put("menuId", item.getMenu().getId());
                    dinnerItem.put("name", item.getMenu().getName());
                    dinnerItem.put("quantity", item.getQuantity());
                    dinnerItem.put("unitPrice", item.getMenu().getPrice());
                    itemMap.put("dinnerItem", dinnerItem);
                    
                    // 서빙 스타일 정보
                    Map<String, Object> servingStyle = new HashMap<>();
                    servingStyle.put("styleId", item.getServingStyle().getId());
                    servingStyle.put("name", item.getServingStyle().getName());
                    servingStyle.put("price", item.getServingStyle().getExtraPrice());
                    itemMap.put("servingStyle", servingStyle);
                    
                    // 옵션 정보 (현재는 빈 리스트로 설정)
                    itemMap.put("options", List.of());
                    
                    return itemMap;
                })
                .collect(Collectors.toList());

        response.put("cartItems", cartItemsList);
        
        int totalPrice = cartItems.stream()
                .mapToInt(item -> (item.getMenu().getPrice() + item.getServingStyle().getExtraPrice()) * item.getQuantity())
                .sum();
        response.put("totalPrice", totalPrice);

        return ResponseEntity.ok(response);
    }

    // 디너 아이템 삭제 (특정 장바구니 아이템 삭제)
    @DeleteMapping("/cart/{customerId}/items/{cartItemId}")
    public ResponseEntity<Map<String, Object>> removeCartItem(@PathVariable Long customerId, @PathVariable Long cartItemId) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new IllegalArgumentException("장바구니 아이템을 찾을 수 없습니다: " + cartItemId));
        
        cartItemRepository.delete(cartItem);
        
        // 총금액 재계산
        Optional<Cart> cart = cartRepository.findByCustomer_Id(customerId);
        int totalAmount = 0;
        if (cart.isPresent()) {
            List<CartItem> remainingItems = cartItemRepository.findByCart_Id(cart.get().getId());
            totalAmount = remainingItems.stream()
                    .mapToInt(item -> item.getMenu().getPrice() * item.getQuantity())
                    .sum();
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("deletedCartItemId", cartItemId);
        response.put("totalAmount", totalAmount);

        return ResponseEntity.ok(response);
    }
    
    // 구성량 수량 변경
    @PatchMapping("/cart/{customerId}/items/{cartItemId}/options/{optionId}")
    public ResponseEntity<Map<String, Object>> changeOptionQuantity(
            @PathVariable Long customerId, 
            @PathVariable Long cartItemId, 
            @PathVariable Long optionId,
            @RequestBody CartOptionQuantityChangeRequest request) {
        
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new IllegalArgumentException("장바구니 아이템을 찾을 수 없습니다: " + cartItemId));
        
        MenuOption option = menuOptionRepository.findById(optionId)
                .orElseThrow(() -> new IllegalArgumentException("구성품을 찾을 수 없습니다: " + optionId));
        
        // 현재는 간단한 구현으로 응답만 반환 (실제 수량 변경로직은 필요에 따라 구현)
        Map<String, Object> response = new HashMap<>();
        response.put("cartItemId", cartItemId);
        
        Map<String, Object> optionData = new HashMap<>();
        optionData.put("optionId", optionId);
        optionData.put("name", option.getItemName());
        optionData.put("quantity", request.getQuantityChange());
        optionData.put("unitPrice", option.getItemPrice());
        response.put("option", optionData);
        
        response.put("itemTotal", option.getItemPrice() * request.getQuantityChange());
        response.put("totalAmount", cartItem.getMenu().getPrice() * cartItem.getQuantity() + option.getItemPrice() * request.getQuantityChange());
        
        return ResponseEntity.ok(response);
    }
    
    // 서빙 스타일 제거
    @DeleteMapping("/cart/{customerId}/items/{cartItemId}/serving-style")
    public ResponseEntity<Map<String, Object>> removeServingStyle(
            @PathVariable Long customerId, 
            @PathVariable Long cartItemId) {
        
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new IllegalArgumentException("장바구니 아이템을 찾을 수 없습니다: " + cartItemId));
        
        // 서빙 스타일을 기본값으로 변경(추가 가격 0원으로 설정)
        ServingStyle defaultStyle = servingStyleRepository.findAll().stream()
                .filter(style -> style.getExtraPrice() == 0)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("기본 서빙 스타일을 찾을 수 없습니다"));
        
        cartItem.setServingStyle(defaultStyle);
        cartItemRepository.save(cartItem);
        
        Map<String, Object> response = new HashMap<>();
        response.put("cartItemId", cartItemId);
        response.put("itemTotal", cartItem.getMenu().getPrice() * cartItem.getQuantity());
        
        // 총금액 재계산
        Optional<Cart> cart = cartRepository.findByCustomer_Id(customerId);
        int totalAmount = 0;
        if (cart.isPresent()) {
            List<CartItem> cartItems = cartItemRepository.findByCart_Id(cart.get().getId());
            totalAmount = cartItems.stream()
                    .mapToInt(item -> item.getMenu().getPrice() * item.getQuantity() + item.getServingStyle().getExtraPrice())
                    .sum();
        }
        response.put("totalAmount", totalAmount);
        
        return ResponseEntity.ok(response);
    }
}
