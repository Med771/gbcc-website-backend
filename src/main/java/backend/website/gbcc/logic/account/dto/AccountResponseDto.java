package backend.website.gbcc.logic.account.dto;

import backend.website.gbcc.model.AccountRole;

import java.time.Instant;
import java.util.UUID;

public record AccountResponseDto(
        UUID id,
        String name,
        String phone,
        String email,
        AccountRole role,
        Boolean isBlocked,
        Instant createdAt,
        Instant updatedAt
) {
}
