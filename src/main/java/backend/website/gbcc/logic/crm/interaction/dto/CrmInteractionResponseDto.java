package backend.website.gbcc.logic.crm.interaction.dto;

import java.time.Instant;
import java.util.UUID;

public record CrmInteractionResponseDto(
        UUID id,
        Instant createdAt,
        Instant updatedAt,
        UUID organizationId,
        UUID leadId,
        Instant occurredAt,
        UUID authorAccountId,
        String authorDisplayName,
        String resultNote,
        String commentText,
        String nextStep,
        UUID nextTaskId
) {
}
