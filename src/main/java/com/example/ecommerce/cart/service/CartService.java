package com.example.ecommerce.cart.service;

import com.example.ecommerce.cart.dto.*;
import com.example.ecommerce.cart.entity.CartItem;
import com.example.ecommerce.cart.repository.CartItemRepository;
import com.example.ecommerce.common.exception.BusinessException;
import com.example.ecommerce.product.entity.Product;
import com.example.ecommerce.product.repository.ProductRepository;
import com.example.ecommerce.user.entity.User;
import com.example.ecommerce.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Transactional
    public CartItemResponse addToCart(Long userId, CartItemRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "用户不存在"));

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new BusinessException("PRODUCT_NOT_FOUND", "商品不存在"));

        if (product.getStockQuantity() < request.getQuantity()) {
            throw new BusinessException("INSUFFICIENT_STOCK", "商品库存不足");
        }

        if (product.getStatus() != Product.ProductStatus.ACTIVE) {
            throw new BusinessException("PRODUCT_NOT_AVAILABLE", "商品不可用");
        }

        // 检查购物车中是否已有该商品
        CartItem cartItem = cartItemRepository.findByUserIdAndProductId(userId, request.getProductId())
                .map(existingItem -> {
                    // 更新数量
                    int newQuantity = existingItem.getQuantity() + request.getQuantity();
                    if (newQuantity > product.getStockQuantity()) {
                        throw new BusinessException("INSUFFICIENT_STOCK", "商品库存不足");
                    }
                    existingItem.setQuantity(newQuantity);
                    return existingItem;
                })
                .orElseGet(() -> {
                    // 创建新的购物车项
                    return CartItem.builder()
                            .user(user)
                            .product(product)
                            .quantity(request.getQuantity())
                            .selected(request.getSelected() != null ? request.getSelected() : true)
                            .build();
                });

        CartItem savedCartItem = cartItemRepository.save(cartItem);
        return mapToResponse(savedCartItem);
    }

    @Transactional(readOnly = true)
    public CartResponse getMyCart(Long userId) {
        List<CartItem> cartItems = cartItemRepository.findByUserId(userId);
        
        List<CartItemResponse> itemResponses = cartItems.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        int totalItems = cartItems.size();
        int totalSelectedItems = (int) cartItems.stream().filter(CartItem::getSelected).count();
        
        BigDecimal totalAmount = cartItems.stream()
                .map(item -> item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal selectedTotalAmount = cartItems.stream()
                .filter(CartItem::getSelected)
                .map(item -> item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CartResponse.builder()
                .items(itemResponses)
                .totalItems(totalItems)
                .totalSelectedItems(totalSelectedItems)
                .totalAmount(totalAmount)
                .selectedTotalAmount(selectedTotalAmount)
                .build();
    }

    @Transactional
    public CartItemResponse updateCartItemQuantity(Long userId, Long cartItemId, UpdateCartItemRequest request) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new BusinessException("CART_ITEM_NOT_FOUND", "购物车项不存在"));

        if (!cartItem.getUser().getId().equals(userId)) {
            throw new BusinessException("UNAUTHORIZED", "无权操作此购物车项");
        }

        Product product = cartItem.getProduct();
        if (request.getQuantity() > product.getStockQuantity()) {
            throw new BusinessException("INSUFFICIENT_STOCK", "商品库存不足");
        }

        cartItem.setQuantity(request.getQuantity());
        CartItem updatedCartItem = cartItemRepository.save(cartItem);
        return mapToResponse(updatedCartItem);
    }

    @Transactional
    public void deleteCartItem(Long userId, Long cartItemId) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new BusinessException("CART_ITEM_NOT_FOUND", "购物车项不存在"));

        if (!cartItem.getUser().getId().equals(userId)) {
            throw new BusinessException("UNAUTHORIZED", "无权操作此购物车项");
        }

        cartItemRepository.delete(cartItem);
    }

    @Transactional
    public void batchDeleteCartItems(Long userId, BatchDeleteRequest request) {
        cartItemRepository.deleteByIdsAndUserId(request.getCartItemIds(), userId);
    }

    @Transactional
    public void clearCart(Long userId) {
        cartItemRepository.deleteByUserId(userId);
    }

    @Transactional
    public CartItemResponse updateCartItemSelected(Long userId, Long cartItemId, UpdateSelectedRequest request) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new BusinessException("CART_ITEM_NOT_FOUND", "购物车项不存在"));

        if (!cartItem.getUser().getId().equals(userId)) {
            throw new BusinessException("UNAUTHORIZED", "无权操作此购物车项");
        }

        cartItem.setSelected(request.getSelected());
        CartItem updatedCartItem = cartItemRepository.save(cartItem);
        return mapToResponse(updatedCartItem);
    }

    private CartItemResponse mapToResponse(CartItem cartItem) {
        Product product = cartItem.getProduct();
        BigDecimal subtotal = product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));

        return CartItemResponse.builder()
                .id(cartItem.getId())
                .productId(product.getId())
                .productName(product.getName())
                .productDescription(product.getDescription())
                .productImageUrl(product.getImageUrl())
                .productPrice(product.getPrice())
                .quantity(cartItem.getQuantity())
                .subtotal(subtotal)
                .selected(cartItem.getSelected())
                .createdAt(cartItem.getCreatedAt())
                .updatedAt(cartItem.getUpdatedAt())
                .build();
    }
}
