package backend.website.gbcc.logic.crm.lead.dto;

import backend.website.gbcc.logic.crm.lead.CrmLeadStatus;

import java.time.Instant;
import java.util.UUID;

public record CrmLeadResponseDto(
        UUID id,
        Instant createdAt,
        Instant updatedAt,
        String companyName,
        String phones,
        String presumedContacts,
        String managerComment,
        String department,
        CrmLeadStatus status,
        UUID assignedToAccountId,
        String assignedToDisplayName,
        UUID convertedOrganizationId
) {
}
