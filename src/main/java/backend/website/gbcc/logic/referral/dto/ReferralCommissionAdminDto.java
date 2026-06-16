package backend.website.gbcc.logic.referral.dto;

import backend.website.gbcc.model.ReferralClientType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ReferralCommissionAdminDto(
        UUID id,
        Instant createdAt,
        UUID orderId,
        Long orderDisplayNumber,
        UUID referrerAccountId,
        String referrerEmail,
        UUID refereeAccountId,
        String refereeEmail,
        BigDecimal orderAmount,
        BigDecimal commissionPercent,
        BigDecimal commissionAmount,
        ReferralClientType clientType
) {
}
