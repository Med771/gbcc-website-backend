package backend.website.gbcc.logic.crm.supply.dto;

import backend.website.gbcc.logic.crm.supply.CrmSupplyStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CreateCrmSupplyRequestDto(
        @NotNull UUID organizationId,
        @NotNull LocalDate supplyAt,
        @NotBlank String productDescription,
        BigDecimal quantity,
        @NotNull CrmSupplyStatus status,
        String commentText,
        Double deliveryLatitude,
        Double deliveryLongitude,
        UUID contractLineId
) {
}
