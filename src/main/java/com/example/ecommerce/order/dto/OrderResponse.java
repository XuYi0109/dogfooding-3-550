package com.example.ecommerce.order.dto;

import com.example.ecommerce.order.entity.Order;
import com.example.ecommerce.order.entity.ShippingAddress;
import com.example.ecommerce.user.dto.UserResponse;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class OrderResponse {
    private Long id;
    private String orderNumber;
    private UserResponse user;
    private BigDecimal totalAmount;
    private Order.OrderStatus status;
    private List<OrderItemResponse> items;
    private ShippingAddress shippingAddress;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    @Data
    @Builder
    public static class OrderItemResponse {
        private Long id;
        private ProductResponse product;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal totalPrice;
    }
    
    @Data
    @Builder
    public static class ProductResponse {
        private Long id;
        private String name;
        private String description;
        private BigDecimal price;
        private String imageUrl;
    }
}