package backend.website.gbcc.logic.order.dto;

import backend.website.gbcc.model.OrderStatus;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record UpdateOrderStatusRequestDto(
        @NotNull(message = "status is required")
        OrderStatus status,
        Instant estimatedDeliveryAt,
        Instant estimatedDeliveryEnd,
        String receiptUrl,
        String comment
) {
}
