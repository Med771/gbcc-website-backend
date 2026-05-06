package backend.website.gbcc.logic.crm.contract.dto;

import backend.website.gbcc.logic.crm.contract.CrmContractLineStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record CrmContractLineResponseDto(
        UUID id,
        Instant createdAt,
        Instant updatedAt,
        UUID contractId,
        LocalDate plannedDate,
        BigDecimal plannedQuantity,
        CrmContractLineStatus lineStatus,
        String commentText
) {
}
