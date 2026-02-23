package backend.website.gbcc.logic.product.dto;

import java.math.BigDecimal;

public record ProductSearchRequestDto(
        String className,
        String seriesName,
        String typeName,
        String brand,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        Boolean isActive
) {
}
