package backend.website.gbcc.logic.crm.contract.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record CrmContractResponseDto(
        UUID id,
        Instant createdAt,
        Instant updatedAt,
        UUID organizationId,
        LocalDate startDate,
        LocalDate endDate,
        String commentText
) {
}
