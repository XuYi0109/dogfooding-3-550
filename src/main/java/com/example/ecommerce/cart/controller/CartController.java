package com.example.ecommerce.cart.controller;

import com.example.ecommerce.cart.dto.*;
import com.example.ecommerce.cart.service.CartService;
import com.example.ecommerce.common.response.ApiResponse;
import com.example.ecommerce.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @PostMapping("/items")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<CartItemResponse>> addToCart(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody CartItemRequest request) {
        CartItemResponse response = cartService.addToCart(userPrincipal.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("商品已添加到购物车", response));
    }

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<CartResponse>> getMyCart(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        CartResponse response = cartService.getMyCart(userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/items/{cartItemId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<CartItemResponse>> updateCartItemQuantity(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long cartItemId,
            @Valid @RequestBody UpdateCartItemRequest request) {
        CartItemResponse response = cartService.updateCartItemQuantity(userPrincipal.getId(), cartItemId, request);
        return ResponseEntity.ok(ApiResponse.success("购物车商品数量已更新", response));
    }

    @DeleteMapping("/items/{cartItemId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<Object>> deleteCartItem(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long cartItemId) {
        cartService.deleteCartItem(userPrincipal.getId(), cartItemId);
        return ResponseEntity.ok(ApiResponse.success("购物车商品已删除"));
    }

    @PostMapping("/items/batch-delete")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<Object>> batchDeleteCartItems(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody BatchDeleteRequest request) {
        cartService.batchDeleteCartItems(userPrincipal.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("购物车商品已批量删除"));
    }

    @DeleteMapping("/clear")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<Object>> clearCart(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        cartService.clearCart(userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success("购物车已清空"));
    }

    @PatchMapping("/items/{cartItemId}/selected")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<CartItemResponse>> updateCartItemSelected(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long cartItemId,
            @Valid @RequestBody UpdateSelectedRequest request) {
        CartItemResponse response = cartService.updateCartItemSelected(userPrincipal.getId(), cartItemId, request);
        return ResponseEntity.ok(ApiResponse.success("商品选中状态已更新", response));
    }
}
