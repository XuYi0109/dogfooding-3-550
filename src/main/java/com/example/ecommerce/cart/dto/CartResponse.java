package com.example.ecommerce.cart.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartResponse {
    
    private List<CartItemResponse> items;
    private Integer totalItems;
    private Integer totalSelectedItems;
    private BigDecimal totalAmount;
    private BigDecimal selectedTotalAmount;
}
