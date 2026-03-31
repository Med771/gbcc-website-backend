package backend.website.gbcc.logic.referral.dto;

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
        BigDecimal commissionAmount
) {
}
