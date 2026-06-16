package backend.website.gbcc.logic.product.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record PatchProductStockRequestDto(
        @NotNull(message = "stockQuantity is required")
        @DecimalMin(value = "0", message = "stockQuantity must be greater or equal to 0")
        Integer stockQuantity
) {
}
