package backend.website.gbcc.logic.product.dto;

import java.util.UUID;

public record ProductStockResponseDto(
        UUID id,
        String className,
        String brand,
        Integer heightMm,
        Integer widthMm,
        Integer lengthMm,
        Integer stockQuantity,
        Boolean isActive
) {
}
