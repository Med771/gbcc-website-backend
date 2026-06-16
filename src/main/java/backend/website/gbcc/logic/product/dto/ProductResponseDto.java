package backend.website.gbcc.logic.product.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ProductResponseDto(
        UUID id,
        String className,
        String seriesName,
        String brand,
        String description,
        String tagline,
        String tags,
        Integer interestCount,
        String deliveryText,
        String licensesText,
        Integer popularityScore,
        Integer heightMm,
        Integer widthMm,
        Integer lengthMm,
        BigDecimal weightKg,
        BigDecimal price,
        BigDecimal originalPrice,
        BigDecimal finalPrice,
        String priceLabel,
        String priceLabelHint,
        BigDecimal discountPercent,
        BigDecimal discountedPrice,
        Integer stockQuantity,
        Boolean isActive,
        List<UUID> photoFileIds,
        Instant createdAt,
        Instant updatedAt
) {
}
