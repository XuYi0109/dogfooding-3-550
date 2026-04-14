package com.example.ecommerce.cart.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateSelectedRequest {
    
    @NotNull(message = "选中状态不能为空")
    private Boolean selected;
}
