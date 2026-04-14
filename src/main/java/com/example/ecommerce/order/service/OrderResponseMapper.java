package com.example.ecommerce.order.service;

import com.example.ecommerce.order.dto.OrderResponse;
import com.example.ecommerce.order.entity.Order;
import com.example.ecommerce.order.entity.OrderItem;
import com.example.ecommerce.user.dto.UserResponse;

import java.util.stream.Collectors;

public class OrderResponseMapper {

    public static OrderResponse toResponse(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .user(UserResponse.fromEntity(order.getUser()))
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .items(order.getOrderItems().stream()
                        .map(OrderResponseMapper::toItemResponse)
                        .collect(Collectors.toList()))
                .shippingAddress(order.getShippingAddress())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }

    private static OrderResponse.OrderItemResponse toItemResponse(OrderItem orderItem) {
        return OrderResponse.OrderItemResponse.builder()
                .id(orderItem.getId())
                .product(OrderResponse.ProductResponse.builder()
                        .id(orderItem.getProduct().getId())
                        .name(orderItem.getProduct().getName())
                        .description(orderItem.getProduct().getDescription())
                        .price(orderItem.getProduct().getPrice())
                        .imageUrl(orderItem.getProduct().getImageUrl())
                        .build())
                .quantity(orderItem.getQuantity())
                .unitPrice(orderItem.getUnitPrice())
                .totalPrice(orderItem.getTotalPrice())
                .build();
    }
}