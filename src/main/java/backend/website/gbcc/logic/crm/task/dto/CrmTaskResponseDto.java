package backend.website.gbcc.logic.crm.task.dto;

import backend.website.gbcc.logic.crm.task.CrmTaskStatus;

import java.time.Instant;
import java.util.UUID;

public record CrmTaskResponseDto(
        UUID id,
        Instant createdAt,
        Instant updatedAt,
        UUID organizationId,
        UUID leadId,
        Instant dueAt,
        String reason,
        String commentText,
        CrmTaskStatus status,
        UUID assigneeAccountId,
        String assigneeDisplayName,
        boolean overdue
) {
}
