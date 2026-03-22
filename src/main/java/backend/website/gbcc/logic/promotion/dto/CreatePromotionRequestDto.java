package backend.website.gbcc.logic.promotion.dto;

import backend.website.gbcc.model.PromotionScopeType;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record CreatePromotionRequestDto(
        @NotBlank(message = "name is required")
        String name,
        @NotNull(message = "discountPercent is required")
        @DecimalMin(value = "0.0", message = "discountPercent must be between 0 and 100")
        @DecimalMax(value = "100.0", message = "discountPercent must be between 0 and 100")
        BigDecimal discountPercent,
        Instant validFrom,
        Instant validTo,
        @NotNull(message = "isActive is required")
        Boolean isActive,
        @NotNull(message = "priority is required")
        Integer priority,
        @NotNull(message = "scope is required")
        PromotionScopeType scope,
        UUID scopeReferenceId
) {
}
