package backend.website.gbcc.logic.order.dto;

import backend.website.gbcc.model.OrderStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record PatchOrderManagerRequestDto(
        String contactName,
        String contactPhone,
        String contactEmail,
        String managerNotes,
        BigDecimal deliveryFee,
        UUID crmOrganizationId
) {
}
