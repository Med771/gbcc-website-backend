package backend.website.gbcc.logic.cooperationrequest.dto;

import java.time.Instant;
import java.util.UUID;

public record CooperationRequestResponseDto(
        UUID id,
        String name,
        String phone,
        String email,
        String cooperationType,
        String comment,
        UUID attachmentFileId,
        Boolean consentProcessing,
        UUID assignedToAccountId,
        String assignedToName,
        String assignedToEmail,
        Instant assignedAt,
        Instant createdAt,
        Instant updatedAt
) {
}
