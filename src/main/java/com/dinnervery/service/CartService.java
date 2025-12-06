package com.dinnervery.service;

import com.dinnervery.dto.cart.request.CartAddItemRequest;
import com.dinnervery.dto.cart.request.CartOptionQuantityChangeRequest;
import com.dinnervery.entity.Cart;
import com.dinnervery.entity.CartItem;
import com.dinnervery.entity.Customer;
import com.dinnervery.entity.CartItemOption;
import com.dinnervery.repository.CartItemRepository;
import com.dinnervery.repository.CartRepository;
import com.dinnervery.repository.CustomerRepository;
import com.dinnervery.repository.CartItemOptionRepository;
import com.dinnervery.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final CustomerRepository customerRepository;
    private final CartItemOptionRepository cartItemOptionRepository;
    private final StorageService storageService;

    @Transactional
    public Map<String, Object> addItemToCart(Long customerId, CartAddItemRequest request) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("고객을 찾을 수 없습니다: " + customerId));

        Optional<Cart> existingCart = cartRepository.findByCustomer_Id(customerId);
        Cart cart = existingCart.orElseGet(() -> {
            Cart newCart = Cart.builder().customer(customer).build();
            return cartRepository.save(newCart);
        });

        CartItem cartItem = CartItem.builder()
                .menuId(request.getMenuId())
                .menuName(request.getMenuName())
                .menuPrice(request.getMenuPrice())
                .styleId(request.getStyleId())
                .styleName(request.getStyleName())
                .styleExtraPrice(request.getStyleExtraPrice())
                .quantity(request.getMenuQuantity())
                .build();

        cartItem.setCart(cart);
        if (request.getOptions() != null) {
            for (CartAddItemRequest.OptionRequest optReq : request.getOptions()) {
                // StorageService에서 옵션 이름에 따른 재고 소비량 조회
                int storageConsumption = storageService.getStorageConsumption(optReq.getOptionName());
                
                CartItemOption cartItemOption = CartItemOption.builder()
                        .optionId(optReq.getOptionId())
                        .optionName(optReq.getOptionName())
                        .optionPrice(optReq.getOptionPrice())
                        .defaultQty(optReq.getDefaultQty())
                        .quantity(optReq.getQuantity())
                        .storageConsumption(storageConsumption)
                        .build();
                cartItem.addCartItemOption(cartItemOption);
            }
        }
        CartItem savedCartItem = cartItemRepository.save(cartItem);
        cart.addCartItem(savedCartItem);
        cartRepository.save(cart);

        Map<String, Object> menuObj = new HashMap<>();
        menuObj.put("menuId", savedCartItem.getMenuId());
        menuObj.put("name", savedCartItem.getMenuName());
        menuObj.put("quantity", savedCartItem.getQuantity());
        menuObj.put("unitPrice", savedCartItem.getMenuPrice());

        Map<String, Object> styleObj = new HashMap<>();
        styleObj.put("styleId", savedCartItem.getStyleId());
        styleObj.put("name", savedCartItem.getStyleName());
        styleObj.put("price", savedCartItem.getStyleExtraPrice());

        List<Map<String, Object>> optionsList = savedCartItem.getCartItemOptions().stream()
                .map(o -> {
                    Map<String, Object> optionMap = new HashMap<>();
                    optionMap.put("optionId", o.getOptionId());
                    optionMap.put("name", o.getOptionName());
                    optionMap.put("quantity", o.getQuantity());
                    optionMap.put("unitPrice", o.getOptionPrice());
                    return optionMap;
                })
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("cartItemId", savedCartItem.getId());
        response.put("menu", menuObj);
        response.put("style", styleObj);
        response.put("options", optionsList);
        response.put("totalAmount", savedCartItem.getItemTotalPrice());

        return response;
    }

    public Map<String, Object> getCart(Long customerId) {
        Optional<Cart> cart = cartRepository.findByCustomer_Id(customerId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("cartId", cart.map(Cart::getId).orElse(null));
        response.put("customerId", customerId);
        
        if (cart.isEmpty()) {
            response.put("cartItems", List.of());
            response.put("totalAmount", 0);
            return response;
        }

        List<CartItem> cartItems = cartItemRepository.findByCart_Id(cart.get().getId());

        List<Map<String, Object>> cartItemsList = cartItems.stream()
                .map(item -> {
                    Map<String, Object> itemMap = new HashMap<>();
                    itemMap.put("cartItemId", item.getId());
                    
                    Map<String, Object> dinnerItem = new HashMap<>();
                    dinnerItem.put("menuId", item.getMenuId());
                    dinnerItem.put("name", item.getMenuName());
                    dinnerItem.put("quantity", item.getQuantity());
                    dinnerItem.put("unitPrice", item.getMenuPrice());
                    itemMap.put("dinnerItem", dinnerItem);
                    
                    Map<String, Object> style = new HashMap<>();
                    style.put("styleId", item.getStyleId());
                    style.put("name", item.getStyleName());
                    style.put("extraPrice", item.getStyleExtraPrice());
                    itemMap.put("style", style);
                    
                    List<Map<String, Object>> options = item.getCartItemOptions().stream()
                            .map(o -> {
                                Map<String, Object> m = new HashMap<>();
                                m.put("optionId", o.getOptionId());
                                m.put("name", o.getOptionName());
                                m.put("quantity", o.getQuantity());
                                m.put("defaultQty", o.getDefaultQty());
                                m.put("unitPrice", o.getOptionPrice());
                                m.put("extraPrice", o.calculateExtraCost());
                                return m;
                            }).collect(Collectors.toList());
                    itemMap.put("options", options);
                    
                    return itemMap;
                })
                .collect(Collectors.toList());

        response.put("cartItems", cartItemsList);
        
        int totalAmount = cartItems.stream().mapToInt(CartItem::getItemTotalPrice).sum();
        response.put("totalAmount", totalAmount);

        return response;
    }

    @Transactional
    public Map<String, Object> updateCartItem(Long customerId, Long cartItemId, CartAddItemRequest request) {
        // 고객 검증
        customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("고객을 찾을 수 없습니다: " + customerId));

        // 장바구니 아이템 조회 및 소유권 검증
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new IllegalArgumentException("장바구니 아이템을 찾을 수 없습니다: " + cartItemId));
        
        // 해당 cartItem이 해당 customer의 cart에 속하는지 확인
        Cart cart = cartItem.getCart();
        if (cart == null || !cart.getCustomer().getId().equals(customerId)) {
            throw new IllegalArgumentException("해당 장바구니 아이템에 대한 권한이 없습니다.");
        }

        // 기존 옵션 삭제 및 아이템 정보 업데이트
        cartItem.updateItem(
                request.getMenuId(),
                request.getMenuName(),
                request.getMenuPrice(),
                request.getStyleId(),
                request.getStyleName(),
                request.getStyleExtraPrice(),
                request.getMenuQuantity()
        );

        // 새로운 옵션 추가
        if (request.getOptions() != null) {
            for (CartAddItemRequest.OptionRequest optReq : request.getOptions()) {
                // StorageService에서 옵션 이름에 따른 재고 소비량 조회
                int storageConsumption = storageService.getStorageConsumption(optReq.getOptionName());
                
                CartItemOption cartItemOption = CartItemOption.builder()
                        .optionId(optReq.getOptionId())
                        .optionName(optReq.getOptionName())
                        .optionPrice(optReq.getOptionPrice())
                        .defaultQty(optReq.getDefaultQty())
                        .quantity(optReq.getQuantity())
                        .storageConsumption(storageConsumption)
                        .build();
                cartItem.addCartItemOption(cartItemOption);
            }
        }

        CartItem savedCartItem = cartItemRepository.save(cartItem);

        // 응답 생성
        Map<String, Object> menuObj = new HashMap<>();
        menuObj.put("menuId", savedCartItem.getMenuId());
        menuObj.put("name", savedCartItem.getMenuName());
        menuObj.put("quantity", savedCartItem.getQuantity());
        menuObj.put("unitPrice", savedCartItem.getMenuPrice());

        Map<String, Object> styleObj = new HashMap<>();
        styleObj.put("styleId", savedCartItem.getStyleId());
        styleObj.put("name", savedCartItem.getStyleName());
        styleObj.put("price", savedCartItem.getStyleExtraPrice());

        List<Map<String, Object>> optionsList = savedCartItem.getCartItemOptions().stream()
                .map(o -> {
                    Map<String, Object> optionMap = new HashMap<>();
                    optionMap.put("optionId", o.getOptionId());
                    optionMap.put("name", o.getOptionName());
                    optionMap.put("quantity", o.getQuantity());
                    optionMap.put("unitPrice", o.getOptionPrice());
                    return optionMap;
                })
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("cartItemId", savedCartItem.getId());
        response.put("menu", menuObj);
        response.put("style", styleObj);
        response.put("options", optionsList);
        response.put("totalAmount", savedCartItem.getItemTotalPrice());

        return response;
    }

    @Transactional
    public Map<String, Object> changeOptionQuantity(
            Long customerId, 
            Long cartItemId, 
            Long optionId,
            CartOptionQuantityChangeRequest request) {
        
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new IllegalArgumentException("장바구니 아이템을 찾을 수 없습니다: " + cartItemId));
        
        CartItemOption cartItemOption = cartItemOptionRepository
                .findByCartItem_IdAndOptionId(cartItemId, optionId)
                .orElseThrow(() -> new IllegalArgumentException("장바구니 옵션을 찾을 수 없습니다: " + optionId));

        if (request.getQuantity() == null || request.getQuantity() < 1) {
            throw new IllegalArgumentException("수량은 1 이상이어야 합니다.");
        }
        
        cartItemOption.updateQuantity(request.getQuantity());
        cartItemRepository.save(cartItem);

        Map<String, Object> response = new HashMap<>();
        response.put("cartItemId", cartItemId);
        
        Map<String, Object> optionData = new HashMap<>();
        optionData.put("optionId", cartItemOption.getOptionId());
        optionData.put("name", cartItemOption.getOptionName());
        optionData.put("quantity", cartItemOption.getQuantity());
        optionData.put("unitPrice", cartItemOption.getOptionPrice());
        response.put("option", optionData);

        response.put("itemTotal", cartItem.getItemTotalPrice());
        
        Optional<Cart> cart = cartRepository.findByCustomer_Id(customerId);
        int totalAmount = 0;
        if (cart.isPresent()) {
            List<CartItem> items = cartItemRepository.findByCart_Id(cart.get().getId());
            totalAmount = items.stream().mapToInt(CartItem::getItemTotalPrice).sum();
        }
        response.put("totalAmount", totalAmount);

        return response;
    }
}

