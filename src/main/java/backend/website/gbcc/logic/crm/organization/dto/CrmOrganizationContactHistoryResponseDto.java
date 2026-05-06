package backend.website.gbcc.logic.crm.organization.dto;

import java.time.Instant;
import java.util.UUID;

public record CrmOrganizationContactHistoryResponseDto(
        UUID id,
        Instant createdAt,
        UUID contactId,
        UUID changedByAccountId,
        String changedByDisplayName,
        String previousSnapshot,
        String newSnapshot
) {
}
