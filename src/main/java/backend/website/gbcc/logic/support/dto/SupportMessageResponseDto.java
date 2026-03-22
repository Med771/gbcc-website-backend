package backend.website.gbcc.logic.support.dto;

import backend.website.gbcc.model.SupportMessageAuthorType;

import java.time.Instant;
import java.util.UUID;

public record SupportMessageResponseDto(
        UUID id,
        SupportMessageAuthorType authorType,
        UUID authorAccountId,
        String body,
        Instant createdAt
) {
}
