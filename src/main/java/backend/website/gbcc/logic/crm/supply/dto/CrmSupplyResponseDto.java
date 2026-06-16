package backend.website.gbcc.logic.crm.supply.dto;

import backend.website.gbcc.logic.crm.supply.CrmSupplyStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record CrmSupplyResponseDto(
        UUID id,
        Instant createdAt,
        Instant updatedAt,
        UUID organizationId,
        LocalDate supplyAt,
        LocalTime supplyTime,
        String productDescription,
        BigDecimal quantity,
        CrmSupplyStatus status,
        String commentText,
        String deliveryAddress,
        String objectName,
        Double deliveryLatitude,
        Double deliveryLongitude,
        UUID branchId,
        UUID productId,
        UUID contractLineId
) {
}
