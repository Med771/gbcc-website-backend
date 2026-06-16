package backend.website.gbcc.logic.crm.organization.dto;

import java.time.Instant;
import java.util.UUID;

public record CrmOrganizationBranchResponseDto(
        UUID id,
        Instant createdAt,
        Instant updatedAt,
        UUID organizationId,
        String name,
        String address,
        boolean isDefault
) {
}
