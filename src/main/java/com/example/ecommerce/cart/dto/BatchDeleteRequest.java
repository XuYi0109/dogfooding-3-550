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
    
    @NotEmpty(message = "购物车项ID列表不能为空")
    private List<Long> cartItemIds;
}
