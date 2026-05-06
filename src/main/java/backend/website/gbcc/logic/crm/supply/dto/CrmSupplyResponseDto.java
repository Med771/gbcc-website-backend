package backend.website.gbcc.logic.crm.supply.dto;

import backend.website.gbcc.logic.crm.supply.CrmSupplyStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record CrmSupplyResponseDto(
        UUID id,
        Instant createdAt,
        Instant updatedAt,
        UUID organizationId,
        LocalDate supplyAt,
        String productDescription,
        BigDecimal quantity,
        CrmSupplyStatus status,
        String commentText,
        Double deliveryLatitude,
        Double deliveryLongitude,
        UUID contractLineId
) {
}
