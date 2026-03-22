package backend.website.gbcc.logic.product.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ProductResponseDto(
        UUID id,
        String className,
        String seriesName,
        String typeName,
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
        BigDecimal discountPercent,
        BigDecimal discountedPrice,
        Boolean isActive,
        List<UUID> photoFileIds,
        Instant createdAt,
        Instant updatedAt
) {
}
