package backend.website.gbcc.logic.order.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderItemResponseDto(
        UUID id,
        UUID productId,
        String productBrand,
        String productClassName,
        String productSeriesName,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal unitDiscountPercent,
        BigDecimal unitDiscountedPrice,
        BigDecimal lineTotalPrice,
        BigDecimal lineTotalDiscountedPrice
) {
}
