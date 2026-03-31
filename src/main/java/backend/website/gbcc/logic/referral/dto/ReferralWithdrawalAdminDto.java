package backend.website.gbcc.logic.referral.dto;

import backend.website.gbcc.model.ReferralWithdrawalStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ReferralWithdrawalAdminDto(
        UUID id,
        Instant createdAt,
        UUID accountId,
        String accountEmail,
        BigDecimal amount,
        ReferralWithdrawalStatus status,
        Instant processedAt,
        UUID processedByAccountId,
        String processedByEmail,
        String adminNote
) {
}
