package com.dinnervery.service;

import com.dinnervery.dto.cart.request.CartAddItemRequest;
import com.dinnervery.dto.cart.request.CartOptionQuantityChangeRequest;
import com.dinnervery.entity.Cart;
import com.dinnervery.entity.CartItem;
import com.dinnervery.entity.Customer;
import com.dinnervery.entity.Menu;
import com.dinnervery.entity.MenuOption;
import com.dinnervery.entity.Style;
import com.dinnervery.entity.CartItemOption;
import com.dinnervery.repository.CartItemRepository;
import com.dinnervery.repository.CartRepository;
import com.dinnervery.repository.CustomerRepository;
import com.dinnervery.repository.MenuRepository;
import com.dinnervery.repository.MenuOptionRepository;
import com.dinnervery.repository.StyleRepository;
import com.dinnervery.repository.CartItemOptionRepository;
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
    private final MenuRepository menuRepository;
    private final MenuOptionRepository menuOptionRepository;
    private final StyleRepository styleRepository;
    private final CartItemOptionRepository cartItemOptionRepository;

    @Transactional
    public Map<String, Object> addItemToCart(Long customerId, CartAddItemRequest request) {
        // 고객 조회
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("고객을 찾을 수 없습니다: " + customerId));

        // 메뉴 조회
        Menu menu = menuRepository.findById(request.getMenuId())
                .orElseThrow(() -> new IllegalArgumentException("메뉴를 찾을 수 없습니다: " + request.getMenuId()));

        // 스타일 조회
        Style style = styleRepository.findById(request.getStyleId())
                .orElseThrow(() -> new IllegalArgumentException("스타일을 찾을 수 없습니다: " + request.getStyleId()));

        // 장바구니 조회 또는 생성
        Optional<Cart> existingCart = cartRepository.findByCustomer_Id(customerId);
        Cart cart = existingCart.orElseGet(() -> {
            Cart newCart = Cart.builder().customer(customer).build();
            return cartRepository.save(newCart);
        });

        // 장바구니 아이템 생성
        CartItem cartItem = CartItem.builder()
                .menu(menu)
                .style(style)
                .quantity(request.getMenuQuantity())
                .build();

        // CartItem에 Cart 설정
        cartItem.setCart(cart);
        // 옵션 생성 및 추가
        if (request.getOptions() != null) {
            for (CartAddItemRequest.OptionRequest optReq : request.getOptions()) {
                MenuOption menuOption = menuOptionRepository.findById(optReq.getOptionId())
                        .orElseThrow(() -> new IllegalArgumentException("구성품을 찾을 수 없습니다: " + optReq.getOptionId()));
                CartItemOption cartItemOption = CartItemOption.builder()
                        .menuOption(menuOption)
                        .quantity(optReq.getQuantity())
                        .build();
                cartItem.addCartItemOption(cartItemOption);
            }
        }
        // CartItem을 먼저 저장하여 ID 할당
        CartItem savedCartItem = cartItemRepository.save(cartItem);
        // Cart에 추가 (이미 저장된 CartItem이므로 cascade 저장은 발생하지 않음)
        cart.addCartItem(savedCartItem);
        cartRepository.save(cart);

        // 메뉴 객체
        Map<String, Object> menuObj = new HashMap<>();
        menuObj.put("menuId", savedCartItem.getMenu().getId());
        menuObj.put("name", savedCartItem.getMenu().getName());
        menuObj.put("quantity", savedCartItem.getQuantity());
        menuObj.put("unitPrice", savedCartItem.getMenu().getPrice());

        // 스타일 객체
        Map<String, Object> styleObj = new HashMap<>();
        styleObj.put("styleId", savedCartItem.getStyle().getId());
        styleObj.put("name", savedCartItem.getStyle().getName());
        styleObj.put("price", savedCartItem.getStyle().getExtraPrice());

        // 옵션 배열
        List<Map<String, Object>> optionsList = savedCartItem.getCartItemOptions().stream()
                .map(o -> {
                    Map<String, Object> optionMap = new HashMap<>();
                    optionMap.put("optionId", o.getMenuOption().getId());
                    optionMap.put("name", o.getMenuOption().getName());
                    optionMap.put("quantity", o.getQuantity());
                    optionMap.put("unitPrice", o.getMenuOption().getPrice());
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
                    
                    // 디너 아이템 정보
                    Map<String, Object> dinnerItem = new HashMap<>();
                    dinnerItem.put("menuId", item.getMenu().getId());
                    dinnerItem.put("name", item.getMenu().getName());
                    dinnerItem.put("quantity", item.getQuantity());
                    dinnerItem.put("unitPrice", item.getMenu().getPrice());
                    itemMap.put("dinnerItem", dinnerItem);
                    
                    // 스타일 정보
                    Map<String, Object> style = new HashMap<>();
                    style.put("styleId", item.getStyle().getId());
                    style.put("name", item.getStyle().getName());
                    style.put("extraPrice", item.getStyle().getExtraPrice());
                    itemMap.put("style", style);
                    
                    // 옵션 정보
                    List<Map<String, Object>> options = item.getCartItemOptions().stream()
                            .map(o -> {
                                Map<String, Object> m = new HashMap<>();
                                m.put("optionId", o.getMenuOption().getId());
                                m.put("name", o.getMenuOption().getName());
                                m.put("quantity", o.getQuantity());
                                m.put("defaultQty", o.getMenuOption().getDefaultQty());
                                m.put("unitPrice", o.getMenuOption().getPrice());
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
    public Map<String, Object> changeOptionQuantity(
            Long customerId, 
            Long cartItemId, 
            Long optionId,
            CartOptionQuantityChangeRequest request) {
        
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new IllegalArgumentException("장바구니 아이템을 찾을 수 없습니다: " + cartItemId));
        
        MenuOption option = menuOptionRepository.findById(optionId)
                .orElseThrow(() -> new IllegalArgumentException("구성품을 찾을 수 없습니다: " + optionId));
        
        CartItemOption cartItemOption = cartItemOptionRepository
                .findByCartItem_IdAndMenuOption_Id(cartItemId, optionId)
                .orElseThrow(() -> new IllegalArgumentException("장바구니 옵션을 찾을 수 없습니다: " + optionId));

        // 수량은 1 이상이어야 함 (비즈니스 로직)
        if (request.getQuantity() == null || request.getQuantity() < 1) {
            throw new IllegalArgumentException("수량은 1 이상이어야 합니다.");
        }
        
        cartItemOption.updateQuantity(request.getQuantity());
        cartItemRepository.save(cartItem);

        Map<String, Object> response = new HashMap<>();
        response.put("cartItemId", cartItemId);
        
        // 옵션 객체
        Map<String, Object> optionData = new HashMap<>();
        optionData.put("optionId", optionId);
        optionData.put("name", option.getName());
        optionData.put("quantity", cartItemOption.getQuantity());
        optionData.put("unitPrice", option.getPrice());
        response.put("option", optionData);

        response.put("itemTotal", cartItem.getItemTotalPrice());
        
        // 총금액 재계산
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

