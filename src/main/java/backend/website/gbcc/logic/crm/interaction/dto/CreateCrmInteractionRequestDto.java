package backend.website.gbcc.logic.crm.interaction.dto;

import backend.website.gbcc.logic.crm.interaction.CrmInteractionType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.UUID;

public record CreateCrmInteractionRequestDto(
        UUID organizationId,
        UUID leadId,
        @NotNull Instant occurredAt,
        CrmInteractionType interactionType,
        @Size(max = 512) String resultNote,
        String commentText,
        String nextStep,
        UUID nextTaskId
) {
}
