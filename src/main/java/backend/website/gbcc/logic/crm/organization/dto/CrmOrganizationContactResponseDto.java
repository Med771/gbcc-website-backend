package backend.website.gbcc.logic.crm.organization.dto;

import java.time.Instant;
import java.util.UUID;

public record CrmOrganizationContactResponseDto(
        UUID id,
        Instant createdAt,
        Instant updatedAt,
        UUID organizationId,
        String fullName,
        String department,
        String positionTitle,
        String socialLinks,
        String phone,
        String email,
        String extraNote
) {
}
