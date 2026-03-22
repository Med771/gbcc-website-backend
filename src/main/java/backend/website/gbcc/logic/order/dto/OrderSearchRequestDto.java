package backend.website.gbcc.logic.order.dto;

import backend.website.gbcc.model.OrderStatus;

import java.util.UUID;

public record OrderSearchRequestDto(
        UUID customerId,
        OrderStatus status
) {
}
