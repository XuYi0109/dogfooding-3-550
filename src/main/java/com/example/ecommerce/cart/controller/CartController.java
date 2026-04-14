package com.example.ecommerce.cart.controller;

import com.example.ecommerce.cart.dto.AddToCartRequest;
import com.example.ecommerce.cart.dto.BatchDeleteRequest;
import com.example.ecommerce.cart.dto.CartItemResponse;
import com.example.ecommerce.cart.dto.CartResponse;
import com.example.ecommerce.cart.dto.UpdateCartQuantityRequest;
import com.example.ecommerce.cart.service.CartService;
import com.example.ecommerce.common.response.ApiResponse;
import com.example.ecommerce.user.entity.User;
import com.example.ecommerce.user.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final UserRepository userRepository;

    @PostMapping("/items")
    public ResponseEntity<ApiResponse<CartItemResponse>> addToCart(
            Authentication authentication,
            @Valid @RequestBody AddToCartRequest request) {
        Long userId = getUserId(authentication);
        CartItemResponse cartItem = cartService.addToCart(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Product added to cart successfully", cartItem));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<CartResponse>> getCart(Authentication authentication) {
        Long userId = getUserId(authentication);
        CartResponse cart = cartService.getCart(userId);
        return ResponseEntity.ok(ApiResponse.success(cart));
    }

    @PutMapping("/items/{cartItemId}")
    public ResponseEntity<ApiResponse<CartItemResponse>> updateCartItemQuantity(
            Authentication authentication,
            @PathVariable Long cartItemId,
            @Valid @RequestBody UpdateCartQuantityRequest request) {
        Long userId = getUserId(authentication);
        CartItemResponse cartItem = cartService.updateQuantity(userId, cartItemId, request);
        return ResponseEntity.ok(ApiResponse.success("Cart item quantity updated successfully", cartItem));
    }

    @DeleteMapping("/items/{cartItemId}")
    public ResponseEntity<ApiResponse<Object>> deleteCartItem(
            Authentication authentication,
            @PathVariable Long cartItemId) {
        Long userId = getUserId(authentication);
        cartService.deleteCartItem(userId, cartItemId);
        return ResponseEntity.ok(ApiResponse.success("Cart item deleted successfully"));
    }

    @DeleteMapping("/items/batch")
    public ResponseEntity<ApiResponse<Object>> batchDeleteCartItems(
            Authentication authentication,
            @Valid @RequestBody BatchDeleteRequest request) {
        Long userId = getUserId(authentication);
        cartService.batchDeleteCartItems(userId, request.getCartItemIds());
        return ResponseEntity.ok(ApiResponse.success("Cart items deleted successfully"));
    }

    @DeleteMapping("/clear")
    public ResponseEntity<ApiResponse<Object>> clearCart(Authentication authentication) {
        Long userId = getUserId(authentication);
        cartService.clearCart(userId);
        return ResponseEntity.ok(ApiResponse.success("Cart cleared successfully"));
    }

    @PatchMapping("/items/{cartItemId}/select")
    public ResponseEntity<ApiResponse<CartItemResponse>> toggleSelect(
            Authentication authentication,
            @PathVariable Long cartItemId) {
        Long userId = getUserId(authentication);
        CartItemResponse cartItem = cartService.toggleSelected(userId, cartItemId);
        return ResponseEntity.ok(ApiResponse.success("Cart item selection toggled successfully", cartItem));
    }

    private Long getUserId(Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getId();
    }
}
