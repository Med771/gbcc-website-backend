package backend.website.gbcc.logic.support.dto;

import backend.website.gbcc.model.SupportConversationStatus;

import java.time.Instant;
import java.util.UUID;

public record CreateSupportConversationResponseDto(
        UUID id,
        String guestAccessToken,
        String subject,
        SupportConversationStatus status,
        Instant createdAt
) {
}
