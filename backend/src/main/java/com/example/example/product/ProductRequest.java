package com.example.example.product;

import java.math.BigDecimal;
import java.util.List;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProductRequest(
        @NotBlank @Size(max = 160) String name,
        @Size(max = 2000) String description,
        @NotNull @DecimalMin(value = "0.00") BigDecimal price,
        @Min(0) int stockQuantity,
        @NotBlank @Size(max = 80) String category,
        @Size(max = 8) List<@NotBlank @Size(max = 500) String> imageUrls,
        Boolean active
) {
}
