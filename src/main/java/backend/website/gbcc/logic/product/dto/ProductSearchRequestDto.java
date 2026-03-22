package backend.website.gbcc.logic.product.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record ProductSearchRequestDto(
        String query,
        String className,
        UUID classId,
        List<UUID> seriesIds,
        List<UUID> typeIds,
        String seriesName,
        String typeName,
        String brand,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        Integer minHeightMm,
        Integer maxHeightMm,
        Boolean isActive
) {
}
