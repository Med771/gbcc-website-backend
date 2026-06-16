package backend.website.gbcc.logic.product.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record CreateProductRequestDto(
        @NotBlank(message = "className is required")
        String className,
        String seriesName,
        @NotBlank(message = "brand is required")
        String brand,
        String description,
        String tagline,
        String tags,
        String deliveryText,
        String licensesText,
        @NotNull(message = "heightMm is required")
        @Positive(message = "heightMm must be greater than 0")
        Integer heightMm,
        @NotNull(message = "widthMm is required")
        @Positive(message = "widthMm must be greater than 0")
        Integer widthMm,
        @NotNull(message = "lengthMm is required")
        @Positive(message = "lengthMm must be greater than 0")
        Integer lengthMm,
        @NotNull(message = "weightKg is required")
        @DecimalMin(value = "0.001", message = "weightKg must be greater than 0")
        BigDecimal weightKg,
        @NotNull(message = "price is required")
        @DecimalMin(value = "0.0", message = "price must be greater or equal to 0")
        BigDecimal price,
        @DecimalMin(value = "0.0", message = "discountPercent must be greater or equal to 0")
        BigDecimal discountPercent,
        Boolean isActive,
        @DecimalMin(value = "0", message = "stockQuantity must be greater or equal to 0")
        Integer stockQuantity
) {
}
