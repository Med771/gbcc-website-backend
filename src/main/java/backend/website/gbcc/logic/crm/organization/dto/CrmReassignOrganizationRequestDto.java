package backend.website.gbcc.logic.crm.organization.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CrmReassignOrganizationRequestDto(
        @NotNull UUID assignedToAccountId
) {
}
