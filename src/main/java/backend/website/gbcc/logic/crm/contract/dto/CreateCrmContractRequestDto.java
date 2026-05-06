package backend.website.gbcc.logic.crm.contract.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record CreateCrmContractRequestDto(
        @NotNull UUID organizationId,
        LocalDate startDate,
        LocalDate endDate,
        String commentText
) {
}
