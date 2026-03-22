package backend.website.gbcc.logic.order.dto;

import backend.website.gbcc.model.OrderStatus;

import java.time.Instant;
import java.util.UUID;

public record OrderStatusHistoryResponseDto(
        UUID id,
        OrderStatus fromStatus,
        OrderStatus toStatus,
        UUID changedByAccountId,
        String comment,
        Instant estimatedDeliveryAt,
        Instant createdAt
) {
}
