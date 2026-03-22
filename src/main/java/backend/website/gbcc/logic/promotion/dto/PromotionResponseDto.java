package backend.website.gbcc.logic.promotion.dto;

import backend.website.gbcc.model.PromotionScopeType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PromotionResponseDto(
        UUID id,
        String name,
        BigDecimal discountPercent,
        Instant validFrom,
        Instant validTo,
        Boolean isActive,
        Integer priority,
        PromotionScopeType scope,
        UUID scopeReferenceId,
        Instant createdAt,
        Instant updatedAt
) {
}
