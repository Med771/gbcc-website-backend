package backend.website.gbcc.logic.support.dto;

import backend.website.gbcc.model.SupportConversationStatus;

import java.time.Instant;
import java.util.UUID;

public record SupportConversationSummaryResponseDto(
        UUID id,
        String subject,
        SupportConversationStatus status,
        String guestName,
        String guestEmail,
        UUID customerId,
        Instant createdAt,
        Instant updatedAt
) {
}
