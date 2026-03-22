package backend.website.gbcc.logic.promotion.dto;

import backend.website.gbcc.model.PromotionScopeType;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PatchPromotionRequestDto(
        String name,
        @DecimalMin(value = "0.0", message = "discountPercent must be between 0 and 100")
        @DecimalMax(value = "100.0", message = "discountPercent must be between 0 and 100")
        BigDecimal discountPercent,
        Instant validFrom,
        Instant validTo,
        Boolean isActive,
        Integer priority,
        PromotionScopeType scope,
        UUID scopeReferenceId
) {
}
