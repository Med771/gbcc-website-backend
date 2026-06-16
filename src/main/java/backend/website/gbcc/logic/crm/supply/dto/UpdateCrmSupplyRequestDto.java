package backend.website.gbcc.logic.crm.supply.dto;

import backend.website.gbcc.logic.crm.supply.CrmSupplyStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record UpdateCrmSupplyRequestDto(
        @NotNull LocalDate supplyAt,
        LocalTime supplyTime,
        @NotBlank String productDescription,
        BigDecimal quantity,
        @NotNull CrmSupplyStatus status,
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
