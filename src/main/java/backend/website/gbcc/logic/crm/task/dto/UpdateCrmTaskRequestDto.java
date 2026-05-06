package backend.website.gbcc.logic.crm.task.dto;

import backend.website.gbcc.logic.crm.task.CrmTaskStatus;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

public record UpdateCrmTaskRequestDto(
        UUID organizationId,
        UUID leadId,
        @NotNull Instant dueAt,
        String reason,
        String commentText,
        @NotNull CrmTaskStatus status,
        UUID assigneeAccountId
) {
}
