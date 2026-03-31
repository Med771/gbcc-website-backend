package backend.website.gbcc.logic.referral.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ReferralWithdrawRequestDto(
        @NotNull(message = "amount is required")
        @DecimalMin(value = "0.01", message = "amount must be positive")
        @Digits(integer = 12, fraction = 2)
        BigDecimal amount
) {
}
