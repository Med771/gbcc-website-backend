package backend.website.gbcc.logic.referral.dto;

import java.time.Instant;
import java.util.UUID;

public record ReferralRegistrationAdminDto(
        UUID refereeAccountId,
        String refereeEmail,
        Instant refereeRegisteredAt,
        UUID referrerAccountId,
        String referrerEmail,
        String referrerReferralCode
) {
}
