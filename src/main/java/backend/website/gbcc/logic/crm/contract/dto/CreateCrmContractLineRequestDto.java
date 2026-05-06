package backend.website.gbcc.logic.crm.contract.dto;

import backend.website.gbcc.logic.crm.contract.CrmContractLineStatus;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateCrmContractLineRequestDto(
        LocalDate plannedDate,
        BigDecimal plannedQuantity,
        @NotNull CrmContractLineStatus lineStatus,
        String commentText
) {
}
