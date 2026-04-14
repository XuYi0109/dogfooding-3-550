package com.example.ecommerce.cart.service;

import com.example.ecommerce.cart.dto.AddToCartRequest;
import com.example.ecommerce.cart.dto.CartItemResponse;
import com.example.ecommerce.cart.dto.CartResponse;
import com.example.ecommerce.cart.dto.UpdateCartQuantityRequest;
import com.example.ecommerce.cart.entity.CartItem;
import com.example.ecommerce.cart.repository.CartRepository;
import com.example.ecommerce.common.exception.BusinessException;
import com.example.ecommerce.product.entity.Product;
import com.example.ecommerce.product.repository.ProductRepository;
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

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;

    @Transactional
    public CartItemResponse addToCart(Long userId, AddToCartRequest request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new BusinessException("PRODUCT_NOT_FOUND", "Product not found"));

        if (product.getStatus() != Product.ProductStatus.ACTIVE) {
            throw new BusinessException("PRODUCT_NOT_AVAILABLE", "Product is not available");
        }

        if (product.getStockQuantity() < request.getQuantity()) {
            throw new BusinessException("INSUFFICIENT_STOCK", 
                "Insufficient stock. Available: " + product.getStockQuantity());
        }

        CartItem cartItem = cartRepository.findByUserIdAndProductId(userId, request.getProductId())
                .map(existingItem -> {
                    int newQuantity = existingItem.getQuantity() + request.getQuantity();
                    if (product.getStockQuantity() < newQuantity) {
                        throw new BusinessException("INSUFFICIENT_STOCK", 
                            "Insufficient stock. Available: " + product.getStockQuantity());
                    }
                    existingItem.setQuantity(newQuantity);
                    return existingItem;
                })
                .orElseGet(() -> CartItem.builder()
                        .userId(userId)
                        .productId(request.getProductId())
                        .quantity(request.getQuantity())
                        .selected(true)
                        .build());

        CartItem savedItem = cartRepository.save(cartItem);
        log.info("Product {} added to cart for user {}", request.getProductId(), userId);
        
        return buildCartItemResponse(savedItem, product);
    }

    @Transactional(readOnly = true)
    public CartResponse getCart(Long userId) {
        List<CartItem> cartItems = cartRepository.findByUserId(userId);
        
        List<CartItemResponse> itemResponses = cartItems.stream()
                .map(item -> {
                    Product product = productRepository.findById(item.getProductId())
                            .orElse(null);
                    if (product == null) {
                        return null;
                    }
                    return buildCartItemResponse(item, product);
                })
                .filter(item -> item != null)
                .collect(Collectors.toList());

        BigDecimal totalPrice = itemResponses.stream()
                .map(CartItemResponse::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal selectedTotalPrice = itemResponses.stream()
                .filter(CartItemResponse::isSelected)
                .map(CartItemResponse::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int selectedItemsCount = (int) itemResponses.stream()
                .filter(CartItemResponse::isSelected)
                .count();

        return CartResponse.builder()
                .items(itemResponses)
                .totalItems(itemResponses.size())
                .totalPrice(totalPrice)
                .selectedTotalPrice(selectedTotalPrice)
                .selectedItemsCount(selectedItemsCount)
                .build();
    }

    @Transactional
    public CartItemResponse updateQuantity(Long userId, Long cartItemId, UpdateCartQuantityRequest request) {
        CartItem cartItem = cartRepository.findByIdAndUserId(cartItemId, userId)
                .orElseThrow(() -> new BusinessException("CART_ITEM_NOT_FOUND", "Cart item not found"));

        Product product = productRepository.findById(cartItem.getProductId())
                .orElseThrow(() -> new BusinessException("PRODUCT_NOT_FOUND", "Product not found"));

        if (product.getStockQuantity() < request.getQuantity()) {
            throw new BusinessException("INSUFFICIENT_STOCK", 
                "Insufficient stock. Available: " + product.getStockQuantity());
        }

        cartItem.setQuantity(request.getQuantity());
        CartItem updatedItem = cartRepository.save(cartItem);
        
        log.info("Cart item {} quantity updated to {} for user {}", cartItemId, request.getQuantity(), userId);
        
        return buildCartItemResponse(updatedItem, product);
    }

    @Transactional
    public void deleteCartItem(Long userId, Long cartItemId) {
        CartItem cartItem = cartRepository.findByIdAndUserId(cartItemId, userId)
                .orElseThrow(() -> new BusinessException("CART_ITEM_NOT_FOUND", "Cart item not found"));
        
        cartRepository.delete(cartItem);
        log.info("Cart item {} deleted for user {}", cartItemId, userId);
    }

    @Transactional
    public void batchDeleteCartItems(Long userId, List<Long> cartItemIds) {
        if (cartItemIds == null || cartItemIds.isEmpty()) {
            throw new BusinessException("INVALID_REQUEST", "Cart item IDs cannot be empty");
        }

        List<CartItem> cartItems = cartRepository.findAllById(cartItemIds).stream()
                .filter(item -> item.getUserId().equals(userId))
                .collect(Collectors.toList());

        if (cartItems.isEmpty()) {
            throw new BusinessException("CART_ITEMS_NOT_FOUND", "No valid cart items found");
        }

        cartRepository.deleteAll(cartItems);
        log.info("Batch deleted {} cart items for user {}", cartItems.size(), userId);
    }

    @Transactional
    public void clearCart(Long userId) {
        cartRepository.deleteByUserId(userId);
        log.info("Cart cleared for user {}", userId);
    }

    @Transactional
    public CartItemResponse toggleSelected(Long userId, Long cartItemId) {
        CartItem cartItem = cartRepository.findByIdAndUserId(cartItemId, userId)
                .orElseThrow(() -> new BusinessException("CART_ITEM_NOT_FOUND", "Cart item not found"));

        cartItem.setSelected(!cartItem.isSelected());
        CartItem updatedItem = cartRepository.save(cartItem);
        
        Product product = productRepository.findById(cartItem.getProductId())
                .orElseThrow(() -> new BusinessException("PRODUCT_NOT_FOUND", "Product not found"));

        log.info("Cart item {} selection toggled to {} for user {}", 
            cartItemId, updatedItem.isSelected(), userId);
        
        return buildCartItemResponse(updatedItem, product);
    }

    private CartItemResponse buildCartItemResponse(CartItem cartItem, Product product) {
        BigDecimal subtotal = product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));
        
        return CartItemResponse.builder()
                .id(cartItem.getId())
                .productId(product.getId())
                .productName(product.getName())
                .productImageUrl(product.getImageUrl())
                .productPrice(product.getPrice())
                .quantity(cartItem.getQuantity())
                .subtotal(subtotal)
                .selected(cartItem.isSelected())
                .stockQuantity(product.getStockQuantity())
                .createdAt(cartItem.getCreatedAt())
                .updatedAt(cartItem.getUpdatedAt())
                .build();
    }
}
