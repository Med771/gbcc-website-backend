package backend.website.gbcc.logic.referral.dto;

import java.time.Instant;
import java.util.UUID;

public record ReferralClickAdminDto(
        UUID id,
        Instant createdAt,
        UUID referrerAccountId,
        String referrerEmail,
        String referrerReferralCode,
        String visitorFingerprint
) {
}
