package com.example.ecommerce.cart.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchDeleteRequest {
    
    @NotEmpty(message = "Cart item IDs cannot be empty")
    private List<Long> cartItemIds;
}
