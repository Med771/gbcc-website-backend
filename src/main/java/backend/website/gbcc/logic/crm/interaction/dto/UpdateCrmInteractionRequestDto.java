package backend.website.gbcc.logic.crm.interaction.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.UUID;

public record UpdateCrmInteractionRequestDto(
        UUID organizationId,
        UUID leadId,
        @NotNull Instant occurredAt,
        @Size(max = 512) String resultNote,
        String commentText,
        String nextStep,
        UUID nextTaskId
) {
}
