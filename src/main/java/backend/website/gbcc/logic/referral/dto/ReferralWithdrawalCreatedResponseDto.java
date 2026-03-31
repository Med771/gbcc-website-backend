package backend.website.gbcc.logic.referral.dto;

import backend.website.gbcc.model.ReferralWithdrawalStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ReferralWithdrawalCreatedResponseDto(
        UUID id,
        BigDecimal amount,
        ReferralWithdrawalStatus status,
        Instant createdAt
) {
}
