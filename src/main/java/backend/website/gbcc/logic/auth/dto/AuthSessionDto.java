package backend.website.gbcc.logic.auth.dto;

import backend.website.gbcc.model.AccountRole;

import java.time.Instant;
import java.util.UUID;

public record AuthSessionDto(
        UUID accountId,
        AccountRole role,
        String accessToken,
        Instant accessTokenExpiresAt,
        String refreshToken,
        Instant refreshTokenExpiresAt
) {
}
