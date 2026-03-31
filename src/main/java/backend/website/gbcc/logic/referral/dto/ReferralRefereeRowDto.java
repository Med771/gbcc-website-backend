package backend.website.gbcc.logic.referral.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ReferralRefereeRowDto(
        UUID refereeAccountId,
        String emailMasked,
        Instant registeredAt,
        BigDecimal totalCommissionFromReferee,
        long purchaseCount
) {
}
