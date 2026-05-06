package backend.website.gbcc.logic.crm.lead.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CrmReassignLeadRequestDto(
        @NotNull UUID assignedToAccountId
) {
}
