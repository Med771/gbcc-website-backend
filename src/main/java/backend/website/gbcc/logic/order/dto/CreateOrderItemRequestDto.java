package backend.website.gbcc.logic.order.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public record CreateOrderItemRequestDto(
        @NotNull(message = "productId is required")
        UUID productId,
        @NotNull(message = "quantity is required")
        @Positive(message = "quantity must be greater than 0")
        Integer quantity
) {
}
