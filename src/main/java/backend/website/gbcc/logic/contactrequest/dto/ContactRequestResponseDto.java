package backend.website.gbcc.logic.contactrequest.dto;

import java.time.Instant;
import java.util.UUID;

public record ContactRequestResponseDto(
        UUID id,
        String name,
        String email,
        String phone,
        String message,
        Boolean consentProcessing,
        UUID assignedToAccountId,
        String assignedToName,
        String assignedToEmail,
        Instant assignedAt,
        Instant createdAt,
        Instant updatedAt
) {
}
