package backend.website.gbcc.logic.support.dto;

import backend.website.gbcc.model.SupportConversationStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record SupportConversationDetailResponseDto(
        UUID id,
        String subject,
        SupportConversationStatus status,
        String guestName,
        String guestEmail,
        UUID customerId,
        Instant createdAt,
        Instant updatedAt,
        List<SupportMessageResponseDto> messages
) {
}
