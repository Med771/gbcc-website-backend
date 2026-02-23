package backend.website.gbcc.logic.auth.dto;

import backend.website.gbcc.model.AccountRole;

import java.time.Instant;
import java.util.UUID;

public record AuthTokenResponseDto(
        UUID accountId,
        AccountRole role,
        Instant accessTokenExpiresAt,
        Instant refreshTokenExpiresAt
) {
}
