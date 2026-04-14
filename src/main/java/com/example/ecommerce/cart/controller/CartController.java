package com.example.ecommerce.cart.controller;

import com.example.ecommerce.cart.dto.AddCartItemRequest;
import com.example.ecommerce.cart.dto.CartItemResponse;
import com.example.ecommerce.cart.dto.CartResponse;
import com.example.ecommerce.cart.dto.UpdateCartItemQuantityRequest;
import com.example.ecommerce.cart.service.CartService;
import com.example.ecommerce.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @PostMapping("/items")
    public ResponseEntity<ApiResponse<CartItemResponse>> addToCart(
            @Valid @RequestBody AddCartItemRequest request,
            Authentication authentication) {
        String username = authentication.getName();
        CartItemResponse cartItemResponse = cartService.addToCart(request, username);
        return ResponseEntity.ok(ApiResponse.success("Product added to cart successfully", cartItemResponse));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<CartResponse>> getMyCart(Authentication authentication) {
        String username = authentication.getName();
        CartResponse cartResponse = cartService.getMyCart(username);
        return ResponseEntity.ok(ApiResponse.success(cartResponse));
    }

    @PutMapping("/items/{id}/quantity")
    public ResponseEntity<ApiResponse<CartItemResponse>> updateQuantity(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCartItemQuantityRequest request,
            Authentication authentication) {
        String username = authentication.getName();
        CartItemResponse cartItemResponse = cartService.updateQuantity(id, request.getQuantity(), username);
        return ResponseEntity.ok(ApiResponse.success("Quantity updated successfully", cartItemResponse));
    }

    @DeleteMapping("/items/{id}")
    public ResponseEntity<ApiResponse<Object>> removeCartItem(
            @PathVariable Long id,
            Authentication authentication) {
        String username = authentication.getName();
        cartService.removeCartItem(id, username);
        return ResponseEntity.ok(ApiResponse.success("Cart item removed successfully"));
    }

    @DeleteMapping("/items/batch")
    public ResponseEntity<ApiResponse<Object>> batchRemoveCartItems(
            @RequestBody List<Long> cartItemIds,
            Authentication authentication) {
        String username = authentication.getName();
        cartService.batchRemoveCartItems(cartItemIds, username);
        return ResponseEntity.ok(ApiResponse.success("Batch removed " + cartItemIds.size() + " cart items successfully"));
    }

    @DeleteMapping("/clear")
    public ResponseEntity<ApiResponse<Object>> clearCart(Authentication authentication) {
        String username = authentication.getName();
        cartService.clearCart(username);
        return ResponseEntity.ok(ApiResponse.success("Cart cleared successfully"));
    }

    @PatchMapping("/items/{id}/toggle-selection")
    public ResponseEntity<ApiResponse<CartItemResponse>> toggleSelection(
            @PathVariable Long id,
            Authentication authentication) {
        String username = authentication.getName();
        CartItemResponse cartItemResponse = cartService.toggleSelection(id, username);
        return ResponseEntity.ok(ApiResponse.success("Selection toggled successfully", cartItemResponse));
    }
}
