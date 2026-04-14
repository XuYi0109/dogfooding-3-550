package com.example.ecommerce.order.dto;

import com.example.ecommerce.order.entity.ShippingAddress;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class OrderRequest {
    
    @NotEmpty(message = "Order items cannot be empty")
    private List<@Valid OrderItemRequest> items;
    
    @NotNull(message = "Shipping address is required")
    @Valid
    private ShippingAddress shippingAddress;
    
    @Data
    public static class OrderItemRequest {
        @NotNull(message = "Product ID is required")
        private Long productId;
        
        @NotNull(message = "Quantity is required")
        private Integer quantity;
    }
}