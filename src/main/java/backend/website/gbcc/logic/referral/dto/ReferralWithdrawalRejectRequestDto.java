package backend.website.gbcc.logic.referral.dto;

import jakarta.validation.constraints.Size;

public record ReferralWithdrawalRejectRequestDto(
        @Size(max = 2000)
        String adminNote
) {
}
