package com.example.ecommerce.cart.service;

import com.example.ecommerce.cart.dto.AddCartItemRequest;
import com.example.ecommerce.cart.dto.CartItemResponse;
import com.example.ecommerce.cart.dto.CartResponse;
import com.example.ecommerce.cart.entity.Cart;
import com.example.ecommerce.cart.repository.CartRepository;
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

    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    @Transactional
    public CartItemResponse addToCart(AddCartItemRequest request, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "User not found"));

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new BusinessException("PRODUCT_NOT_FOUND", "Product not found"));

        if (product.getStatus() != Product.ProductStatus.ACTIVE) {
            throw new BusinessException("PRODUCT_INACTIVE", "Product is not available");
        }

        if (product.getStockQuantity() < request.getQuantity()) {
            throw new BusinessException("INSUFFICIENT_STOCK", "Insufficient stock for product");
        }

        Cart existingCart = cartRepository.findByUserIdAndProductId(user.getId(), product.getId()).orElse(null);

        if (existingCart != null) {
            int newQuantity = existingCart.getQuantity() + request.getQuantity();
            if (product.getStockQuantity() < newQuantity) {
                throw new BusinessException("INSUFFICIENT_STOCK", "Insufficient stock for product");
            }
            existingCart.setQuantity(newQuantity);
            Cart updatedCart = cartRepository.save(existingCart);
            log.info("Updated cart item quantity: {} for user: {}", product.getName(), username);
            return CartItemResponse.fromEntity(updatedCart);
        }

        Cart cart = Cart.builder()
                .user(user)
                .product(product)
                .quantity(request.getQuantity())
                .selected(true)
                .build();

        Cart savedCart = cartRepository.save(cart);
        log.info("Added product to cart: {} for user: {}", product.getName(), username);

        return CartItemResponse.fromEntity(savedCart);
    }

    @Transactional(readOnly = true)
    public CartResponse getMyCart(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "User not found"));

        List<Cart> cartItems = cartRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        List<CartItemResponse> itemResponses = cartItems.stream()
                .map(CartItemResponse::fromEntity)
                .collect(Collectors.toList());

        BigDecimal totalAmount = itemResponses.stream()
                .map(CartItemResponse::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<CartItemResponse> selectedItems = itemResponses.stream()
                .filter(CartItemResponse::getSelected)
                .collect(Collectors.toList());

        BigDecimal selectedAmount = selectedItems.stream()
                .map(CartItemResponse::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CartResponse.builder()
                .items(itemResponses)
                .totalItems(itemResponses.size())
                .totalAmount(totalAmount)
                .selectedItemsCount(selectedItems.size())
                .selectedAmount(selectedAmount)
                .build();
    }

    @Transactional
    public CartItemResponse updateQuantity(Long cartItemId, Integer quantity, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "User not found"));

        Cart cartItem = cartRepository.findById(cartItemId)
                .orElseThrow(() -> new BusinessException("CART_ITEM_NOT_FOUND", "Cart item not found"));

        if (!cartItem.getUser().getId().equals(user.getId())) {
            throw new BusinessException("ACCESS_DENIED", "Access denied to this cart item");
        }

        Product product = cartItem.getProduct();
        if (product.getStockQuantity() < quantity) {
            throw new BusinessException("INSUFFICIENT_STOCK", "Insufficient stock for product");
        }

        cartItem.setQuantity(quantity);
        Cart updatedCart = cartRepository.save(cartItem);
        log.info("Updated cart item quantity: {} -> {} for user: {}", product.getName(), quantity, username);

        return CartItemResponse.fromEntity(updatedCart);
    }

    @Transactional
    public void removeCartItem(Long cartItemId, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "User not found"));

        Cart cartItem = cartRepository.findById(cartItemId)
                .orElseThrow(() -> new BusinessException("CART_ITEM_NOT_FOUND", "Cart item not found"));

        if (!cartItem.getUser().getId().equals(user.getId())) {
            throw new BusinessException("ACCESS_DENIED", "Access denied to this cart item");
        }

        cartRepository.delete(cartItem);
        log.info("Removed cart item: {} for user: {}", cartItem.getProduct().getName(), username);
    }

    @Transactional
    public void batchRemoveCartItems(List<Long> cartItemIds, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "User not found"));

        cartRepository.deleteAllByUserIdAndIdIn(user.getId(), cartItemIds);
        log.info("Batch removed {} cart items for user: {}", cartItemIds.size(), username);
    }

    @Transactional
    public void clearCart(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "User not found"));

        cartRepository.deleteAllByUserId(user.getId());
        log.info("Cleared cart for user: {}", username);
    }

    @Transactional
    public CartItemResponse toggleSelection(Long cartItemId, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "User not found"));

        Cart cartItem = cartRepository.findById(cartItemId)
                .orElseThrow(() -> new BusinessException("CART_ITEM_NOT_FOUND", "Cart item not found"));

        if (!cartItem.getUser().getId().equals(user.getId())) {
            throw new BusinessException("ACCESS_DENIED", "Access denied to this cart item");
        }

        cartItem.setSelected(!cartItem.getSelected());
        Cart updatedCart = cartRepository.save(cartItem);
        log.info("Toggled selection for cart item: {} for user: {}", cartItem.getProduct().getName(), username);

        return CartItemResponse.fromEntity(updatedCart);
    }
}
