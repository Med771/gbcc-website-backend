package backend.website.gbcc.logic.product.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record PatchProductRequestDto(
        String className,
        String seriesName,
        String typeName,
        String brand,
        String description,
        String tagline,
        String tags,
        String deliveryText,
        String licensesText,
        Integer interestCount,
        @Positive(message = "heightMm must be greater than 0")
        Integer heightMm,
        @Positive(message = "widthMm must be greater than 0")
        Integer widthMm,
        @Positive(message = "lengthMm must be greater than 0")
        Integer lengthMm,
        @DecimalMin(value = "0.001", message = "weightKg must be greater than 0")
        BigDecimal weightKg,
        @DecimalMin(value = "0.01", message = "price must be greater than 0")
        BigDecimal price,
        @DecimalMin(value = "0.0", message = "discountPercent must be greater or equal to 0")
        BigDecimal discountPercent,
        Boolean isActive
) {
}
