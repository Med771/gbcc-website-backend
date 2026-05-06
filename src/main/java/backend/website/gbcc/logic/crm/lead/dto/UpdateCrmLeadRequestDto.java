package backend.website.gbcc.logic.crm.lead.dto;

import backend.website.gbcc.logic.crm.lead.CrmLeadStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record UpdateCrmLeadRequestDto(
        @NotBlank @Size(max = 512) String companyName,
        String phones,
        String presumedContacts,
        String managerComment,
        @Size(max = 255) String department,
        @NotNull CrmLeadStatus status,
        UUID assignedToAccountId
) {
}
