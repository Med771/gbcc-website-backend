package backend.website.gbcc.logic.referral.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ReferralClickRequestDto(
        @NotBlank(message = "code is required")
        @Size(max = 64)
        String code,
        @Size(max = 128)
        String visitorFingerprint
) {
}
