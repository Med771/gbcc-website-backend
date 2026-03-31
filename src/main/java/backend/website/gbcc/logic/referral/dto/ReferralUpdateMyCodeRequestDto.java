package backend.website.gbcc.logic.referral.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ReferralUpdateMyCodeRequestDto(
        @NotBlank(message = "code is required")
        @Size(max = 32)
        String code
) {
}
