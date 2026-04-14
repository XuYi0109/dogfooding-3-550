package com.example.ecommerce.order.service;

import com.example.ecommerce.common.exception.BusinessException;
import com.example.ecommerce.order.dto.OrderRequest;
import com.example.ecommerce.order.dto.OrderResponse;
import com.example.ecommerce.order.entity.Order;
import com.example.ecommerce.order.entity.OrderItem;
import com.example.ecommerce.order.repository.OrderRepository;
import com.example.ecommerce.product.entity.Product;
import com.example.ecommerce.product.repository.ProductRepository;
import com.example.ecommerce.user.entity.User;
import com.example.ecommerce.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    @Transactional
    public OrderResponse createOrder(OrderRequest request, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "User not found"));

        Order order = Order.builder()
                .orderNumber(generateOrderNumber())
                .user(user)
                .status(Order.OrderStatus.PENDING)
                .shippingAddress(request.getShippingAddress())
                .build();

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (OrderRequest.OrderItemRequest itemRequest : request.getItems()) {
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new BusinessException("PRODUCT_NOT_FOUND", 
                            "Product not found: " + itemRequest.getProductId()));

            if (product.getStatus() != Product.ProductStatus.ACTIVE) {
                throw new BusinessException("PRODUCT_INACTIVE", 
                        "Product is not available: " + product.getName());
            }

            if (product.getStockQuantity() < itemRequest.getQuantity()) {
                throw new BusinessException("INSUFFICIENT_STOCK", 
                        "Insufficient stock for product: " + product.getName());
            }

            OrderItem orderItem = OrderItem.builder()
                    .product(product)
                    .quantity(itemRequest.getQuantity())
                    .unitPrice(product.getPrice())
                    .build();

            order.addOrderItem(orderItem);
            totalAmount = totalAmount.add(orderItem.getTotalPrice());

            product.setStockQuantity(product.getStockQuantity() - itemRequest.getQuantity());
            if (product.getStockQuantity() == 0) {
                product.setStatus(Product.ProductStatus.OUT_OF_STOCK);
            }
            productRepository.save(product);
        }

        order.setTotalAmount(totalAmount);
        Order savedOrder = orderRepository.save(order);
        
        log.info("Order created successfully: {} for user: {}", savedOrder.getOrderNumber(), username);
        
        return OrderResponseMapper.toResponse(savedOrder);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> getUserOrders(String username, Pageable pageable) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "User not found"));
        
        return orderRepository.findByUser(user, pageable)
                .map(OrderResponseMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long id, String username) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new BusinessException("ORDER_NOT_FOUND", "Order not found"));

        if (!order.getUser().getUsername().equals(username)) {
            throw new BusinessException("ACCESS_DENIED", "Access denied to this order");
        }

        return OrderResponseMapper.toResponse(order);
    }

    @Transactional
    public OrderResponse updateOrderStatus(Long id, Order.OrderStatus status, String username) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new BusinessException("ORDER_NOT_FOUND", "Order not found"));

        if (!order.getUser().getUsername().equals(username)) {
            throw new BusinessException("ACCESS_DENIED", "Access denied to this order");
        }

        if (status == Order.OrderStatus.CANCELLED && 
            order.getStatus().ordinal() >= Order.OrderStatus.SHIPPED.ordinal()) {
            throw new BusinessException("INVALID_STATUS", "Cannot cancel shipped or delivered order");
        }

        order.setStatus(status);
        Order updatedOrder = orderRepository.save(order);
        
        log.info("Order status updated: {} -> {}", order.getOrderNumber(), status);
        
        return OrderResponseMapper.toResponse(updatedOrder);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable)
                .map(OrderResponseMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> getOrdersByStatus(Order.OrderStatus status, Pageable pageable) {
        return orderRepository.findByStatus(status, pageable)
                .map(OrderResponseMapper::toResponse);
    }

    private String generateOrderNumber() {
        String orderNumber;
        do {
            orderNumber = "ORD-" + LocalDateTime.now().getYear() + "-" + 
                         UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        } while (orderRepository.existsByOrderNumber(orderNumber));
        
        return orderNumber;
    }
}