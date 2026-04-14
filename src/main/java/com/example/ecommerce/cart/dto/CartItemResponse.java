package com.example.ecommerce.cart.dto;

import com.example.ecommerce.cart.entity.Cart;
import com.example.ecommerce.product.dto.ProductResponse;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class CartItemResponse {
    private Long id;
    private Long userId;
    private ProductResponse product;
    private Integer quantity;
    private Boolean selected;
    private BigDecimal subtotal;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static CartItemResponse fromEntity(Cart cart) {
        ProductResponse productResponse = ProductResponse.fromEntity(cart.getProduct());
        BigDecimal subtotal = productResponse.getPrice().multiply(BigDecimal.valueOf(cart.getQuantity()));

        return CartItemResponse.builder()
                .id(cart.getId())
                .userId(cart.getUser().getId())
                .product(productResponse)
                .quantity(cart.getQuantity())
                .selected(cart.getSelected())
                .subtotal(subtotal)
                .createdAt(cart.getCreatedAt())
                .updatedAt(cart.getUpdatedAt())
                .build();
    }
}
